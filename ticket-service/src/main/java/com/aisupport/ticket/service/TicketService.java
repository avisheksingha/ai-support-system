package com.aisupport.ticket.service;

import java.time.LocalDateTime;
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
import com.aisupport.ticket.exception.TicketNotFoundException;
import com.aisupport.ticket.mapper.TicketMapper;
import com.aisupport.ticket.outbox.OutboxEventService;
import com.aisupport.ticket.repository.TicketRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private static final String TICKET_NOT_FOUND_MSG = "Ticket not found: ";

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final OutboxEventService outboxEventService;

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

    @Transactional(readOnly = true)
    public TicketResponse getTicketByNumber(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new TicketNotFoundException(
                        TICKET_NOT_FOUND_MSG + ticketNumber));
        return ticketMapper.toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(
                        "Ticket not found with ID: " + id));
        return ticketMapper.toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll()
                .stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByStatus(String status) {
        TicketStatus ticketStatus =
                TicketStatus.valueOf(status.toUpperCase());

        return ticketRepository.findByStatus(ticketStatus)
                .stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Transactional
    public TicketResponse updateTicketStatus(String ticketNumber, String newStatus) {

        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new TicketNotFoundException(
                        TICKET_NOT_FOUND_MSG + ticketNumber));

        ticket.transitionTo(
                TicketStatus.valueOf(newStatus.toUpperCase())
        );

        return ticketMapper.toResponse(ticket);
    }

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
    
    @Transactional
    public TicketResponse updateTicketPriority(String ticketNumber,
                                               String newPriority,
                                               Integer slaHours) {

        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new TicketNotFoundException(
                        TICKET_NOT_FOUND_MSG + ticketNumber));

        ticket.setPriority(
                TicketPriority.valueOf(newPriority.toUpperCase())
        );

        if (slaHours != null) {
            ticket.setSlaHours(slaHours);
        }

        return ticketMapper.toResponse(ticket);
    }
    
    @Transactional
    public void applyRoutingResult(TicketRoutedEvent event) {

        Ticket ticket = ticketRepository.findById(event.getTicketId())
                .orElseThrow(() -> new TicketNotFoundException(TICKET_NOT_FOUND_MSG + event.getTicketId()));
        
        // If already assigned, we assume a manual override and skip auto-assignment
        if (ticket.getStatus() == TicketStatus.ASSIGNED) {
            log.info("Ticket {} already assigned — skipping", ticket.getTicketNumber());
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
}