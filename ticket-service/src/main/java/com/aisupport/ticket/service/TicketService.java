package com.aisupport.ticket.service;

import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.common.dto.AnalysisResultDTO;
import com.aisupport.ticket.client.AIAnalysisServiceClient;
import com.aisupport.ticket.client.AnalysisRequest;
import com.aisupport.ticket.dto.TicketRequest;
import com.aisupport.ticket.dto.TicketResponse;
import com.aisupport.ticket.exception.TicketNotFoundException;
import com.aisupport.ticket.mapper.TicketMapper;
import com.aisupport.ticket.model.Ticket;
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
    private final AIAnalysisServiceClient aiAnalysisWebClient;
    private final ApplicationContext applicationContext;
    
    @Transactional
    public TicketResponse createTicket(TicketRequest request) {
        log.info("Creating ticket for customer: {}", request.getCustomerEmail());
        
        // Create and save ticket
        Ticket ticket = ticketMapper.toEntity(request);
        ticket.setTicketNumber(generateTicketNumber());
        ticket.setStatus(Ticket.TicketStatus.ANALYZING); // Set initial status to ANALYZING
        ticket.setPriority(Ticket.Priority.MEDIUM);
        
        ticket = ticketRepository.save(ticket);
        log.info("Ticket created with number: {}", ticket.getTicketNumber());
        
        // Prepare analysis request
        AnalysisRequest analysisRequest = AnalysisRequest.builder()
                .ticketId(ticket.getId())
                .subject(ticket.getSubject())
                .message(ticket.getMessage())
                .build();
        
        // Call AI Analysis Service Asynchronously
        // Use ApplicationContext to get the proxy of the current bean to ensure @Async works
        applicationContext.getBean(TicketService.class).initiateAnalysis(ticket.getId(), analysisRequest);
        
        // Return response immediately
        return ticketMapper.toResponse(ticket);
    }
    
    @Async
    public void initiateAnalysis(Long ticketId, AnalysisRequest analysisRequest) {
        log.info("Starting async analysis for ticket ID: {}", ticketId);
        try {
            // Block until analysis is complete (now safe in async thread)
            AnalysisResultDTO analysisResult = aiAnalysisWebClient.analyzeTicket(analysisRequest).block();
            
            // Fetch fresh ticket record
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new TicketNotFoundException("Ticket not found during async analysis: " + ticketId));
            
            // Update ticket with analysis results
            ticket.setIntent(analysisResult.getIntent());
            ticket.setSentiment(analysisResult.getSentiment());
            ticket.setUrgency(analysisResult.getUrgency());
            ticket.setStatus(Ticket.TicketStatus.ANALYZED);
            
            ticketRepository.save(ticket);
            log.info("Ticket {} analyzed and updated successfully", ticket.getTicketNumber());
            
        } catch (Exception e) {
            log.error("Failed to analyze ticket asynchronously: {}", ticketId, e);
            // Fallback status
            ticketRepository.findById(ticketId).ifPresent(t -> {
                t.setStatus(Ticket.TicketStatus.NEW); // Revert to NEW if analysis fails
                ticketRepository.save(t);
            });
        }
    }
    
    @Transactional(readOnly = true)
    public TicketResponse getTicketByNumber(String ticketNumber) {
        log.info("Fetching ticket: {}", ticketNumber);
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new TicketNotFoundException(TICKET_NOT_FOUND_MSG + ticketNumber));
        return ticketMapper.toResponse(ticket);
    }
    
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id) {
        log.info("Fetching ticket by ID: {}", id);
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(
                        "Ticket not found with ID: " + id));
        return ticketMapper.toResponse(ticket);
    }
    
    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(ticketMapper::toResponse)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByStatus(String status) {
        Ticket.TicketStatus ticketStatus = Ticket.TicketStatus.valueOf(status.toUpperCase());
        return ticketRepository.findByStatus(ticketStatus).stream()
                .map(ticketMapper::toResponse)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByCustomerEmail(String email) {
        return ticketRepository.findByCustomerEmail(email).stream()
                .map(ticketMapper::toResponse)
                .toList();
    }
    
    @Transactional
    public TicketResponse updateTicketStatus(String ticketNumber, String newStatus) {
        log.info("Updating ticket {} to status: {}", ticketNumber, newStatus);
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new TicketNotFoundException(TICKET_NOT_FOUND_MSG + ticketNumber));
        
        ticket.setStatus(Ticket.TicketStatus.valueOf(newStatus.toUpperCase()));
        ticket = ticketRepository.save(ticket);
        
        return ticketMapper.toResponse(ticket);
    }
    
    @Transactional
    public TicketResponse updateTicketPriority(String ticketNumber, String newPriority) {
        log.info("Updating ticket {} to priority: {}", ticketNumber, newPriority);
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new TicketNotFoundException(TICKET_NOT_FOUND_MSG + ticketNumber));
        
        ticket.setPriority(Ticket.Priority.valueOf(newPriority.toUpperCase()));
        ticket = ticketRepository.save(ticket);
        
        return ticketMapper.toResponse(ticket);
    }
    
    @Transactional
    public TicketResponse assignTicket(String ticketNumber, String assignedTo) {
        log.info("Assigning ticket {} to: {}", ticketNumber, assignedTo);
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new TicketNotFoundException(TICKET_NOT_FOUND_MSG + ticketNumber));
        
        ticket.setAssignedTo(assignedTo);
        ticket.setStatus(Ticket.TicketStatus.ASSIGNED);
        ticket = ticketRepository.save(ticket);
        
        return ticketMapper.toResponse(ticket);
    }
    
    @Transactional
    public void deleteTicket(String ticketNumber) {
        log.info("Deleting ticket: {}", ticketNumber);
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new TicketNotFoundException(TICKET_NOT_FOUND_MSG + ticketNumber));
        
        ticketRepository.delete(ticket);
        log.info("Ticket {} deleted successfully", ticketNumber);
    }
    
    private String generateTicketNumber() {
        return "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}