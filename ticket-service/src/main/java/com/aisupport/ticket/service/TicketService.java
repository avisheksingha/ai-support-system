package com.aisupport.ticket.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.common.enums.TicketPriority;
import com.aisupport.common.enums.TicketStatus;
import com.aisupport.common.enums.UserRole;
import com.aisupport.common.event.AiDecision;
import com.aisupport.common.event.AnalysisResult;
import com.aisupport.common.event.DomainEvent;
import com.aisupport.common.event.EventType;
import com.aisupport.common.event.KnowledgeContext;
import com.aisupport.common.event.RoutingDecision;
import com.aisupport.common.event.TicketCreatedEvent;
import com.aisupport.common.event.TicketOrchestratedEvent;
import com.aisupport.common.event.TicketRagResponseEvent;
import com.aisupport.common.event.TicketRoutedEvent;
import com.aisupport.ticket.dto.request.MessageRequest;
import com.aisupport.ticket.dto.request.TicketRequest;
import com.aisupport.ticket.dto.response.MessageResponse;
import com.aisupport.ticket.dto.response.TicketDashboardSummaryResponse;
import com.aisupport.ticket.dto.response.TicketResponse;
import com.aisupport.ticket.entity.Message;
import com.aisupport.ticket.entity.Ticket;
import com.aisupport.ticket.exception.InvalidTicketInputException;
import com.aisupport.ticket.exception.TicketNotFoundException;
import com.aisupport.ticket.mapper.MessageMapper;
import com.aisupport.ticket.mapper.TicketMapper;
import com.aisupport.ticket.notification.WebSocketNotificationService;
import com.aisupport.ticket.outbox.OutboxEventService;
import com.aisupport.ticket.repository.MessageRepository;
import com.aisupport.ticket.repository.TicketRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
/**
 * Business service for ticket lifecycle operations.
 */
public class TicketService {

    private static final String TICKET_NOT_FOUND_MSG = "Ticket not found: ";
    private static final String AGGREGATE_TYPE = "TICKET";

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final OutboxEventService outboxEventService;
    private final WebSocketNotificationService webSocketNotificationService;

    /**
     * Creates a new ticket and stores a corresponding {@code TicketCreatedEvent}
     * in the outbox for asynchronous downstream processing.
     *
     * @param userId optional user ID of the creator
     * @param request ticket creation request payload
     * @return persisted ticket response
     */
    @Transactional
    public TicketResponse createTicket(String userId, String customerEmail, String userName, TicketRequest request) {

        Ticket ticket = ticketMapper.toEntity(request);
        ticket.setTicketNumber(generateTicketNumber());
        ticket.setStatus(TicketStatus.NEW);
        ticket.setPriority(TicketPriority.MEDIUM);
        
        if (customerEmail != null && !customerEmail.isBlank()) {
            ticket.setCustomerEmail(customerEmail);
            
            if (userName != null && !userName.isBlank()) {
                ticket.setCustomerName(userName);
            } else {
                // Default customerName to email prefix if not provided (could be extracted from another header in the future)
                ticket.setCustomerName(customerEmail.split("@")[0]);
            }
        }
        
        if (userId != null && !userId.isBlank()) {
            try {
                ticket.setCustomerUserId(Long.valueOf(userId));
            } catch (NumberFormatException e) {
                log.warn("Invalid user ID format: {}", userId);
            }
        }
        
        // AI starts immediately after creation
        ticket.transitionTo(TicketStatus.ANALYZING);

        ticket = ticketRepository.save(ticket);

        // NEW: Publish event via Outbox (NOT direct Kafka)
        TicketCreatedEvent event = TicketCreatedEvent.builder()
                .ticketId(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .subject(ticket.getSubject())
                .message(ticket.getMessage())
                .priority(ticket.getPriority() != null ? ticket.getPriority().name() : null)
                .createdAt(Instant.now())
                .build();

        outboxEventService.publishEvent(
        		AGGREGATE_TYPE,
                ticket.getId().toString(),
                EventType.TICKET_CREATED,
                event
        );

        log.info("Ticket {} created and event stored in outbox",
                ticket.getTicketNumber());

        return ticketMapper.toResponse(ticket);
    }

    /**
     * Fetches a ticket by its public ticket number.
     *
     * @param ticketNumber unique ticket number
     * @return ticket response
     */
    @Transactional(readOnly = true)
    public TicketResponse getTicketByNumber(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new TicketNotFoundException(
                        TICKET_NOT_FOUND_MSG + ticketNumber));
        return ticketMapper.toResponse(ticket);
    }

    /**
     * Fetches all messages for a specific ticket.
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getTicketMessages(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new TicketNotFoundException(TICKET_NOT_FOUND_MSG + ticketNumber));
                
        return messageRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId())
                .stream()
                .map(messageMapper::toResponse)
                .toList();
    }
    
    /**
     * Fetches all public messages for a specific ticket if owned by customer.
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getCustomerTicketMessages(String ticketNumber, String customerEmail) {
        Ticket ticket = ticketRepository.findByTicketNumberAndCustomerEmail(ticketNumber, customerEmail)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found or access denied: " + ticketNumber));
                
        return messageRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId())
                .stream()
                .filter(msg -> !msg.isInternal())
                .map(messageMapper::toResponse)
                .toList();
    }
    
    /**
     * Adds a new message to a ticket.
     */
    @Transactional
    public MessageResponse addMessage(String ticketNumber, MessageRequest request, String userRole, String userEmail) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new TicketNotFoundException(TICKET_NOT_FOUND_MSG + ticketNumber));
                
        Message message = messageMapper.toEntity(request);
        message.setTicket(ticket);
        message.setInternal(request.isInternal());
        
        if (userEmail != null) {
            message.setSenderId(userEmail);
            String name = userEmail.contains("@") ? userEmail.substring(0, userEmail.indexOf('@')) : userEmail;
            // capitalize first letter
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            message.setSenderName(name);
        } else {
            message.setSenderName("System");
        }
        
        EventType eventType;
        
        // Determine type based on role/request
        if (request.isInternal()) {
            message.setType("INTERNAL_NOTE");
            eventType = EventType.AGENT_REPLY_ADDED;
        } else if (UserRole.AGENT.name().equals(userRole) || UserRole.ADMIN.name().equals(userRole)) {
            message.setType("AGENT_MESSAGE");
            eventType = EventType.AGENT_REPLY_ADDED;
            
            if (ticket.getStatus() == TicketStatus.ASSIGNED) {
                ticket.transitionTo(TicketStatus.IN_PROGRESS); 
            }
        } else {
            message.setType("CUSTOMER_MESSAGE");
            eventType = EventType.CUSTOMER_REPLY_ADDED;
        }
        
        message = messageRepository.save(message);
        
        MessageResponse response = messageMapper.toResponse(message);
        
        DomainEvent<MessageResponse> event = DomainEvent.<MessageResponse>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .entityType(AGGREGATE_TYPE)
                .entityId(ticket.getId().toString())
                .correlationId(UUID.randomUUID().toString())
                .sourceService("ticket-service")
                .timestamp(Instant.now())
                .payload(response)
                .build();
                
        outboxEventService.publishEvent(AGGREGATE_TYPE, ticket.getId().toString(), eventType, event);
        webSocketNotificationService.broadcastEvent(event, ticketNumber);
        
        return response;
    }

    /**
     * Fetches a ticket by internal database identifier.
     *
     * @param id ticket database id
     * @return ticket response
     */
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(
                        "Ticket not found with ID: " + id));
        return ticketMapper.toResponse(ticket);
    }

    /**
     * Returns all tickets.
     *
     * @return list of ticket responses
     */
    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll()
                .stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    /**
     * Helper class for aggregating dashboard metrics.
     */
    private static class DashboardMetrics {
        long critical = 0;
        long high = 0;
        long medium = 0;
        long low = 0;
        long totalWaitMs = 0;
        long oldestTicketMs = 0;
        long resolvedToday = 0;
        long totalHandleMs = 0;
        long nearSlaBreach = 0;
        long nextSlaBreachMins = Long.MAX_VALUE;
        long totalSlaRemainingMins = 0;
        long activeSlaCount = 0;
    }

    /**
     * Returns a dashboard summary for a specific agent.
     */
    @Transactional(readOnly = true)
    public TicketDashboardSummaryResponse getAgentDashboardSummary(String agentId) {
        List<Ticket> tickets = ticketRepository.findByAssignedTo(agentId);
        DashboardMetrics metrics = new DashboardMetrics();
        
        Instant startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS);
        long nowMs = Instant.now().toEpochMilli();

        for (Ticket t : tickets) {
            if (t.getStatus() == TicketStatus.RESOLVED || t.getStatus() == TicketStatus.CLOSED) {
                processResolvedTicket(t, metrics, startOfDay);
            } else {
                processActiveTicket(t, metrics, nowMs);
            }
        }

        return buildDashboardResponse(tickets.size(), metrics);
    }

    private void processResolvedTicket(Ticket t, DashboardMetrics metrics, Instant startOfDay) {
        if (t.getUpdatedAt() != null && t.getUpdatedAt().isAfter(startOfDay)) {
            metrics.resolvedToday++;
            metrics.totalHandleMs += t.getUpdatedAt().toEpochMilli() - t.getCreatedAt().toEpochMilli();
        }
    }

    private void processActiveTicket(Ticket t, DashboardMetrics metrics, long nowMs) {
        updatePriorityCounts(t, metrics);
        updateWaitTime(t, metrics, nowMs);
        updateSlaMetrics(t, metrics, nowMs);
    }

    private void updatePriorityCounts(Ticket t, DashboardMetrics metrics) {
        if (t.getPriority() == TicketPriority.CRITICAL) metrics.critical++;
        else if (t.getPriority() == TicketPriority.HIGH) metrics.high++;
        else if (t.getPriority() == TicketPriority.MEDIUM) metrics.medium++;
        else metrics.low++;
    }

    private void updateWaitTime(Ticket t, DashboardMetrics metrics, long nowMs) {
        long waitTime = nowMs - t.getCreatedAt().toEpochMilli();
        metrics.totalWaitMs += waitTime;
        if (waitTime > metrics.oldestTicketMs) {
            metrics.oldestTicketMs = waitTime;
        }
    }

    private void updateSlaMetrics(Ticket t, DashboardMetrics metrics, long nowMs) {
        if (t.getSlaHours() != null) {
            long targetMs = t.getCreatedAt().toEpochMilli() + (t.getSlaHours() * 3600000L);
            long remainingMins = (targetMs - nowMs) / 60000L;
            metrics.totalSlaRemainingMins += remainingMins;
            metrics.activeSlaCount++;

            if (remainingMins > 0 && remainingMins < metrics.nextSlaBreachMins) {
                metrics.nextSlaBreachMins = remainingMins;
            }
            if (remainingMins > 0 && remainingMins < 120) { // < 2 hours
                metrics.nearSlaBreach++;
            }
        }
    }

    private TicketDashboardSummaryResponse buildDashboardResponse(int totalTickets, DashboardMetrics metrics) {
        long activeCount = totalTickets - metrics.resolvedToday;
        Long avgWaitMins = activeCount > 0 ? (metrics.totalWaitMs / activeCount) / 60000L : null;
        Long oldestMins = activeCount > 0 ? metrics.oldestTicketMs / 60000L : null;
        Long avgSlaMins = metrics.activeSlaCount > 0 ? metrics.totalSlaRemainingMins / metrics.activeSlaCount : null;
        Long avgHandleMins = metrics.resolvedToday > 0 ? (metrics.totalHandleMs / metrics.resolvedToday) / 60000L : null;

        return TicketDashboardSummaryResponse.builder()
                .assignedToday(activeCount) // roughly using active count
                .totalAssigned(activeCount)
                .critical(metrics.critical)
                .high(metrics.high)
                .medium(metrics.medium)
                .low(metrics.low)
                .averageWaitTimeMins(avgWaitMins)
                .oldestTicketAgeMins(oldestMins)
                .nearSlaBreach(metrics.nearSlaBreach)
                .nextSlaBreachMins(metrics.nextSlaBreachMins == Long.MAX_VALUE ? null : metrics.nextSlaBreachMins)
                .averageRemainingSlaMins(avgSlaMins)
                .resolvedToday(metrics.resolvedToday)
                .averageHandleTimeMins(avgHandleMins)
                .build();
    }

    /**
     * Returns all tickets filtered by status.
     *
     * @param status ticket status string
     * @return list of matching ticket responses
     */
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByStatus(String status) {
        TicketStatus ticketStatus = parseTicketStatus(status);

        return ticketRepository.findByStatus(ticketStatus)
                .stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    /**
     * Returns all tickets belonging to a customer, ordered by newest first.
     *
     * @param customerEmail customer's email
     * @return list of matching ticket responses
     */
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByCustomerEmail(String customerEmail) {
        return ticketRepository.findByCustomerEmailOrderByCreatedAtDesc(customerEmail)
                .stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    /**
     * Fetches a specific ticket for a customer, enforcing ownership.
     *
     * @param ticketNumber unique ticket number
     * @param customerEmail customer's email
     * @return ticket response
     */
    @Transactional(readOnly = true)
    public TicketResponse getCustomerTicketByNumber(String ticketNumber, String customerEmail) {
        Ticket ticket = ticketRepository.findByTicketNumberAndCustomerEmail(ticketNumber, customerEmail)
                .orElseThrow(() -> new TicketNotFoundException(
                        "Ticket not found or access denied: " + ticketNumber));
        return ticketMapper.toResponse(ticket);
    }

    /**
     * Updates ticket status and optional SLA.
     *
     * @param ticketNumber ticket number
     * @param newStatus target status
     * @param slaHours optional SLA override in hours
     * @return updated ticket response
     */
    @Transactional
    public TicketResponse updateTicketStatus(String ticketNumber,
                                             String newStatus,
                                             Integer slaHours) {

        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new TicketNotFoundException(
                        TICKET_NOT_FOUND_MSG + ticketNumber));

        ticket.transitionTo(parseTicketStatus(newStatus));

        if (slaHours != null) {
            ticket.setSlaHours(slaHours);
        }

        return ticketMapper.toResponse(ticket);
    }

    /**
     * Assigns a ticket to an agent/team and transitions it to {@code ASSIGNED}.
     *
     * @param ticketNumber ticket number
     * @param assignedTo assignee identifier
     * @param slaHours optional SLA override in hours
     * @return updated ticket response
     */
    @Transactional
    public TicketResponse assignTicket(String ticketNumber,
                                       String assignedTo,
                                       Integer slaHours) {

        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new TicketNotFoundException(
                        TICKET_NOT_FOUND_MSG + ticketNumber));

        ticket.setAssignedTo(assignedTo);
        ticket.transitionTo(TicketStatus.ASSIGNED);

        if (slaHours != null) {
            ticket.setSlaHours(slaHours);
        }

        return ticketMapper.toResponse(ticket);
    }
    
    /**
     * Updates ticket priority and optional SLA.
     *
     * @param ticketNumber ticket number
     * @param newPriority target priority
     * @param slaHours optional SLA override in hours
     * @return updated ticket response
     */
    @Transactional
    public TicketResponse updateTicketPriority(String ticketNumber,
                                               String newPriority,
                                               Integer slaHours) {

        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new TicketNotFoundException(
                        TICKET_NOT_FOUND_MSG + ticketNumber));

        ticket.setPriority(parseTicketPriority(newPriority));

        if (slaHours != null) {
            ticket.setSlaHours(slaHours);
        }

        return ticketMapper.toResponse(ticket);
    }
    
    @Transactional
    public void applyOrchestratedResult(TicketOrchestratedEvent event) {
    	
        Ticket ticket = ticketRepository.findById(event.ticketId())
                .orElseThrow(() -> new TicketNotFoundException(TICKET_NOT_FOUND_MSG + event.ticketId()));
        
        // At-least-once delivery protection
        if (isStatusAtOrBeyondAssigned(ticket.getStatus())) {
            log.info("Ticket {} already at status {}, skipping orchestration event",
                    ticket.getTicketNumber(),
                    ticket.getStatus());
            return;
        }

        log.info(
                "Applying orchestrated result for ticket {} (workflowExecutionId={}, correlationId={})",
                ticket.getTicketNumber(),
                event.metadata().workflowExecutionId(),
                event.metadata().correlationId());
        
        // -------------------------
        // AI Analysis
        // -------------------------
        if (event.analysis() != null) {
            applyAnalysis(ticket, event.analysis());
            ticket.transitionTo(TicketStatus.ANALYZED);
        }

        // -------------------------
        // Routing
        // -------------------------        
        if (event.routing() != null) {
            applyRouting(ticket, event.routing());
            ticket.transitionTo(TicketStatus.ASSIGNED);
        }

        // -------------------------
        // Knowledge
        // -------------------------
        if (event.knowledge() != null) {
            applyKnowledge(ticket, event.knowledge());
        }

        // -------------------------
        // AI Decision
        // -------------------------
        if (event.aiDecision() != null) {
            applyAiDecision(ticket, event.aiDecision());
        }

        // Hibernate dirty checking will persist changes automatically
        log.info(
                "Ticket {} successfully updated. Final status={}, team={}, priority={}",
                ticket.getTicketNumber(),
                ticket.getStatus(),
                ticket.getAssignedTo(),
                ticket.getPriority());
    }

    private void applyAnalysis(Ticket ticket, AnalysisResult analysis) {
        if (analysis == null) return;
        if (analysis.intent() != null) ticket.setIntent(analysis.intent());
        if (analysis.sentiment() != null) ticket.setSentiment(analysis.sentiment());
        if (analysis.urgency() != null) ticket.setUrgency(analysis.urgency());
    }

    private void applyRouting(Ticket ticket, RoutingDecision routing) {
        if (routing == null) return;
        if (routing.assignToTeam() != null) ticket.setAssignedTo(routing.assignToTeam());
        if (routing.priority() != null) ticket.setPriority(routing.priority());
        if (routing.slaHours() != null) ticket.setSlaHours(routing.slaHours());
    }
    
    private void applyKnowledge(Ticket ticket, KnowledgeContext knowledge) {

        if (knowledge == null) {
            return;
        }

        if (knowledge.knowledgeSummary() != null) {
            ticket.setRagResponse(knowledge.knowledgeSummary());
            ticket.setRagGeneratedAt(Instant.now());
        }
    }

    private void applyAiDecision(Ticket ticket, AiDecision decision) {
        if (decision == null) return;
        if (decision.aiSummary() != null) ticket.setAiSummary(decision.aiSummary());
        if (decision.suggestedReply() != null) ticket.setSuggestedReply(decision.suggestedReply());
        if (decision.confidence() != null) ticket.setAiConfidence(decision.confidence());
    }

    /**
     * Applies routing decision results received from routing-service.
     * Duplicate or out-of-order routing events are treated as no-ops.
     *
     * @param event routing event payload
     */
    @Transactional
    public void applyRoutingResult(TicketRoutedEvent event) {

        Ticket ticket = ticketRepository.findById(event.getTicketId())
                .orElseThrow(() -> new TicketNotFoundException(TICKET_NOT_FOUND_MSG + event.getTicketId()));
        
        // For at-least-once delivery, duplicates/out-of-order events should be no-ops.
        if (isStatusAtOrBeyondAssigned(ticket.getStatus())) {
            log.info("Ticket {} already at status {}, skipping routing event",
                    ticket.getTicketNumber(), ticket.getStatus());
            return;
        }

        // Update AI fields if present
        if (event.getIntent() != null) {
            ticket.setIntent(event.getIntent());
        }

        if (event.getSentiment() != null) {
            ticket.setSentiment(event.getSentiment());
        }

        if (event.getUrgency() != null) {
            ticket.setUrgency(event.getUrgency());
        }

        // Update assignment and priority based on routing result
        ticket.setAssignedTo(event.getAssignToTeam());

        // Update priority if present - direct enum assignment, no valueOf needed
        if (event.getPriority() != null) {
            ticket.setPriority(event.getPriority());
        }
        
        // Update SLA if present
        if (event.getSlaHours() != null) {
            ticket.setSlaHours(event.getSlaHours());
        }

        // transition to ASSIGNED — state machine allows NEW → ASSIGNED directly
        ticket.transitionTo(TicketStatus.ASSIGNED);

        log.info("Ticket {} updated from routing event", ticket.getTicketNumber());
        
        log.info("Routing applied ticketId={} ruleResult team={} priority={} sla={}",
                event.getTicketId(),
                event.getAssignToTeam(),
                event.getPriority(),
                event.getSlaHours());
    }
    
    /**
     * Applies generated RAG response to the ticket.
     *
     * @param event RAG response event payload
     */
    @Transactional
    public void applyRagResponse(TicketRagResponseEvent event) {

        Ticket ticket = ticketRepository.findById(event.getTicketId())
                .orElseThrow(() -> new TicketNotFoundException(
                        TICKET_NOT_FOUND_MSG + event.getTicketId()));

        ticket.setRagResponse(event.getResponse());
        ticket.setRagGeneratedAt(event.getGeneratedAt());

        ticketRepository.save(ticket);

        log.info("RAG response applied to ticket {}", ticket.getTicketNumber());
    }

    private String generateTicketNumber() {
        return "TKT-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    private TicketStatus parseTicketStatus(String status) {
        try {
            return TicketStatus.valueOf(status.toUpperCase());
        } catch (Exception ex) {
            throw new InvalidTicketInputException(
                    "Invalid status '" + status + "'. Allowed: "
                            + Arrays.toString(TicketStatus.values())
            );
        }
    }

    private TicketPriority parseTicketPriority(String priority) {
        try {
            return TicketPriority.valueOf(priority.toUpperCase());
        } catch (Exception ex) {
            throw new InvalidTicketInputException(
                    "Invalid priority '" + priority + "'. Allowed: "
                            + Arrays.toString(TicketPriority.values())
            );
        }
    }

    private boolean isStatusAtOrBeyondAssigned(TicketStatus status) {
        return status == TicketStatus.ASSIGNED
                || status == TicketStatus.IN_PROGRESS
                || status == TicketStatus.RESOLVED
                || status == TicketStatus.CLOSED;
    }
}
