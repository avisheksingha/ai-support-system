package com.aisupport.ticket.service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.common.enums.TicketPriority;
import com.aisupport.common.enums.TicketStatus;
import com.aisupport.common.event.AnalysisResult;
import com.aisupport.common.event.KnowledgeContext;
import com.aisupport.common.event.RoutingDecision;
import com.aisupport.common.event.TicketCreatedEvent;
import com.aisupport.common.event.TicketOrchestratedEvent;
import com.aisupport.common.event.TicketRagResponseEvent;
import com.aisupport.common.event.TicketRoutedEvent;
import com.aisupport.ticket.dto.MessageRequest;
import com.aisupport.ticket.dto.MessageResponse;
import com.aisupport.ticket.dto.TicketRequest;
import com.aisupport.ticket.dto.TicketResponse;
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
                .createdAt(Instant.now())
                .build();

        outboxEventService.publishEvent(
                "TICKET",
                ticket.getId().toString(),
                "TicketCreatedEvent",
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
     * Adds a new message to a ticket.
     */
    @Transactional
    public MessageResponse addMessage(String ticketNumber, MessageRequest request, String userRole) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new TicketNotFoundException(TICKET_NOT_FOUND_MSG + ticketNumber));
                
        Message message = messageMapper.toEntity(request);
        message.setTicket(ticket);
        
        // Determine type based on role/request
        if (request.isInternal()) {
            message.setType("INTERNAL_NOTE");
        } else if ("AGENT".equals(userRole) || "ADMIN".equals(userRole)) {
            message.setType("AGENT_MESSAGE");
            // If ticket is WAITING_FOR_CUSTOMER or similar, maybe update status?
            // For now, let's keep status simple
            if (ticket.getStatus() == TicketStatus.ASSIGNED) {
                ticket.transitionTo(TicketStatus.IN_PROGRESS); // Just an example
            }
        } else {
            message.setType("CUSTOMER_MESSAGE");
        }
        
        message = messageRepository.save(message);
        
        MessageResponse response = messageMapper.toResponse(message);
        
        com.aisupport.common.event.DomainEvent<MessageResponse> event = com.aisupport.common.event.DomainEvent.<MessageResponse>builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(com.aisupport.common.event.EventType.MESSAGE_ADDED)
                .entityType("TICKET")
                .entityId(ticket.getId().toString())
                .correlationId(java.util.UUID.randomUUID().toString())
                .sourceService("ticket-service")
                .timestamp(java.time.Instant.now())
                .payload(response)
                .build();
                
        outboxEventService.publishEvent("TICKET", ticket.getId().toString(), "MESSAGE_ADDED", event);
        webSocketNotificationService.broadcastEvent(event);
        
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
