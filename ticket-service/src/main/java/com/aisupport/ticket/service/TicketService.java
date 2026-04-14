package com.aisupport.ticket.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.common.enums.TicketPriority;
import com.aisupport.common.enums.TicketStatus;
import com.aisupport.common.event.TicketCreatedEvent;
import com.aisupport.common.event.TicketRagResponseEvent;
import com.aisupport.common.event.TicketRoutedEvent;
import com.aisupport.ticket.dto.TicketRequest;
import com.aisupport.ticket.dto.TicketResponse;
import com.aisupport.ticket.entity.Ticket;
import com.aisupport.ticket.exception.InvalidTicketInputException;
import com.aisupport.ticket.exception.TicketNotFoundException;
import com.aisupport.ticket.mapper.TicketMapper;
import com.aisupport.ticket.outbox.OutboxEventService;
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
    private final OutboxEventService outboxEventService;

    /**
     * Creates a new ticket and stores a corresponding {@code TicketCreatedEvent}
     * in the outbox for asynchronous downstream processing.
     *
     * @param request ticket creation request payload
     * @return persisted ticket response
     */
    @Transactional
    public TicketResponse createTicket(TicketRequest request) {

        Ticket ticket = ticketMapper.toEntity(request);
        ticket.setTicketNumber(generateTicketNumber());
        ticket.setStatus(TicketStatus.NEW);
        ticket.setPriority(TicketPriority.MEDIUM);

        ticket = ticketRepository.save(ticket);

        // NEW: Publish event via Outbox (NOT direct Kafka)
        TicketCreatedEvent event = TicketCreatedEvent.builder()
                .ticketId(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .subject(ticket.getSubject())
                .message(ticket.getMessage())
                .createdAt(LocalDateTime.now())
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
