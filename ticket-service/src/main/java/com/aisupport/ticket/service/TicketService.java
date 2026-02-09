package com.aisupport.ticket.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.common.dto.AnalysisResultDTO;
import com.aisupport.ticket.client.AIAnalysisClient;
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
    
    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final AIAnalysisClient aiAnalysisClient;
    
    @Transactional
    public TicketResponse createTicket(TicketRequest request) {
        log.info("Creating ticket for customer: {}", request.getCustomerEmail());
        
        // Create and save ticket
        Ticket ticket = ticketMapper.toEntity(request);
        ticket.setTicketNumber(generateTicketNumber());
        ticket.setStatus(Ticket.TicketStatus.NEW);
        ticket.setPriority(Ticket.Priority.MEDIUM);
        
        ticket = ticketRepository.save(ticket);
        log.info("Ticket created with number: {}", ticket.getTicketNumber());
        
        // Call AI Analysis Service synchronously
        try {
            ticket.setStatus(Ticket.TicketStatus.ANALYZING);
            ticketRepository.save(ticket);
            
            AnalysisRequest analysisRequest = AnalysisRequest.builder()
                    .ticketId(ticket.getId())
                    .subject(ticket.getSubject())
                    .message(ticket.getMessage())
                    .build();
            
            AnalysisResultDTO analysisResult = aiAnalysisClient.analyzeTicket(analysisRequest);
            
            // Update ticket with analysis results
            ticket.setIntent(analysisResult.getIntent());
            ticket.setSentiment(analysisResult.getSentiment());
            ticket.setUrgency(analysisResult.getUrgency());
            ticket.setStatus(Ticket.TicketStatus.ANALYZED);
            
            ticket = ticketRepository.save(ticket);
            log.info("Ticket {} analyzed successfully", ticket.getTicketNumber());
            
        } catch (Exception e) {
            log.error("Failed to analyze ticket: {}", ticket.getTicketNumber(), e);
            ticket.setStatus(Ticket.TicketStatus.NEW);
            ticketRepository.save(ticket);
        }
        
        return ticketMapper.toResponse(ticket);
    }
    
    @Transactional(readOnly = true)
    public TicketResponse getTicketByNumber(String ticketNumber) {
        log.info("Fetching ticket: {}", ticketNumber);
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + ticketNumber));
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
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + ticketNumber));
        
        ticket.setStatus(Ticket.TicketStatus.valueOf(newStatus.toUpperCase()));
        ticket = ticketRepository.save(ticket);
        
        return ticketMapper.toResponse(ticket);
    }
    
    @Transactional
    public TicketResponse updateTicketPriority(String ticketNumber, String newPriority) {
        log.info("Updating ticket {} to priority: {}", ticketNumber, newPriority);
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + ticketNumber));
        
        ticket.setPriority(Ticket.Priority.valueOf(newPriority.toUpperCase()));
        ticket = ticketRepository.save(ticket);
        
        return ticketMapper.toResponse(ticket);
    }
    
    @Transactional
    public TicketResponse assignTicket(String ticketNumber, String assignedTo) {
        log.info("Assigning ticket {} to: {}", ticketNumber, assignedTo);
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + ticketNumber));
        
        ticket.setAssignedTo(assignedTo);
        ticket.setStatus(Ticket.TicketStatus.ASSIGNED);
        ticket = ticketRepository.save(ticket);
        
        return ticketMapper.toResponse(ticket);
    }
    
    @Transactional
    public void deleteTicket(String ticketNumber) {
        log.info("Deleting ticket: {}", ticketNumber);
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + ticketNumber));
        
        ticketRepository.delete(ticket);
        log.info("Ticket {} deleted successfully", ticketNumber);
    }
    
    private String generateTicketNumber() {
        return "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}