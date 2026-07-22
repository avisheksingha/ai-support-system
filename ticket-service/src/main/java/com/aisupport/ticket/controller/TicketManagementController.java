package com.aisupport.ticket.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.ticket.dto.request.MessageRequest;
import com.aisupport.ticket.dto.response.MessageResponse;
import com.aisupport.ticket.dto.response.TicketResponse;
import com.aisupport.ticket.service.TicketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api/v1/tickets", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ticket Management", description = "Internal support ticket management endpoints")
public class TicketManagementController {
    
    private final TicketService ticketService;    

    @Operation(
        summary = "Get ticket by number",
        description = "Retrieves a support ticket by its unique ticket number"
    )
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    @GetMapping("/{ticketNumber}")
    public ResponseEntity<TicketResponse> getTicket(
        @Parameter(description = "Unique ticket number") @PathVariable String ticketNumber) {
        
        log.info("Management requested ticket [{}]", ticketNumber);
        return ResponseEntity.ok(
                ticketService.getTicketByNumber(ticketNumber)
        );
    }

    @Operation(
        summary = "Get ticket by ID",
        description = "Retrieves a support ticket by its database ID"
    )
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    @GetMapping("/id/{id}")
    public ResponseEntity<TicketResponse> getTicketById(
            @Parameter(description = "Database ID of the ticket") @PathVariable Long id) {
        
        log.info("Management requested ticket by id [{}]", id);
        return ResponseEntity.ok(
                ticketService.getTicketById(id)
        );
    }
    
    @Operation(
        summary = "Get all tickets",
        description = "Retrieves a list of all support tickets, with optional filtering by status"
    )
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets(
        @Parameter(description = "Filter by status: NEW, ANALYZING, ANALYZED, ASSIGNED, RESOLVED, CLOSED")
        @RequestParam(required = false) String status) {
        
        log.info("Management requested all tickets, status filter: {}", status);
        if (status != null) {
            return ResponseEntity.ok(
                    ticketService.getTicketsByStatus(status)
            );
        }

        return ResponseEntity.ok(
                ticketService.getAllTickets()
        );
    }
    
    @Operation(
        summary = "Update ticket status",
        description = "Updates the status of a support ticket (e.g., NEW, ANALYZING, ANALYZED, ASSIGNED, RESOLVED, CLOSED)"
    )
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    @PatchMapping("/{ticketNumber}/status")
    public ResponseEntity<TicketResponse> updateStatus(
        @Parameter(description = "Unique ticket number") @PathVariable String ticketNumber,
        @Parameter(description = "New status: NEW, ANALYZING, ANALYZED, ASSIGNED, RESOLVED, CLOSED")
        @RequestParam String status,
        @Parameter(description = "Optional SLA override in hours")
        @RequestParam(required = false) Integer slaHours) {
        
        log.info("Management update status request — ticket: {}, status: {}", ticketNumber, status);
        
        return ResponseEntity.ok(
                ticketService.updateTicketStatus(ticketNumber, status, slaHours)
        );
    }
    
    @Operation(
        summary = "Assign ticket to support agent",
        description = "Assigns a support ticket to a specific agent and updates the ticket status to ASSIGNED"
    )
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    @PatchMapping("/{ticketNumber}/assign")
    public ResponseEntity<TicketResponse> assignTicket(
        @Parameter(description = "Unique ticket number") @PathVariable String ticketNumber,
        @Parameter(description = "Agent identifier or name") @RequestParam String assignedTo,
        @Parameter(description = "Optional SLA override in hours")
        @RequestParam(required = false) Integer slaHours) {
        
        log.info("Management assign ticket request — ticket: {}, agent: {}", ticketNumber, assignedTo);
        
        return ResponseEntity.ok(
                ticketService.assignTicket(ticketNumber, assignedTo, slaHours)
        );
    }

    @Operation(
        summary = "Update ticket priority",
        description = "Updates the priority level of a support ticket"
    )
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    @PatchMapping("/{ticketNumber}/priority")
    public ResponseEntity<TicketResponse> updatePriority(
        @Parameter(description = "Unique ticket number") @PathVariable String ticketNumber,
        @Parameter(description = "New priority: LOW, MEDIUM, HIGH, CRITICAL")
        @RequestParam String priority,
        @Parameter(description = "Optional SLA override in hours")
        @RequestParam(required = false) Integer slaHours) {
        
        log.info("Management update priority request — ticket: {}, priority: {}", ticketNumber, priority);
        
        return ResponseEntity.ok(
                ticketService.updateTicketPriority(ticketNumber, priority, slaHours)
        );
    }
    
    @Operation(
        summary = "Get ticket messages",
        description = "Retrieves all messages for a specific ticket"
    )
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    @GetMapping("/{ticketNumber}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(
        @Parameter(description = "Unique ticket number") @PathVariable String ticketNumber) {
        
        log.info("Management requested messages for ticket: {}", ticketNumber);
        
        return ResponseEntity.ok(
                ticketService.getTicketMessages(ticketNumber)
        );
    }
    
    @Operation(
        summary = "Add a new message",
        description = "Adds a new message (reply or internal note) to a ticket"
    )
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    @PostMapping("/{ticketNumber}/messages")
    public ResponseEntity<MessageResponse> addMessage(
        @Parameter(description = "Unique ticket number") @PathVariable String ticketNumber,
        @Valid @RequestBody MessageRequest request,
        @RequestHeader(value = "X-User-Role", required = true) String userRole,
        @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        
        log.info("Management adding message to ticket: {}", ticketNumber);
        
        return ResponseEntity.ok(
                ticketService.addMessage(ticketNumber, request, userRole, userEmail)
        );
    }
    
    @Operation(
        summary = "Get agent dashboard summary",
        description = "Retrieves aggregated metrics for a specific agent's dashboard"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved agent dashboard summary")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    @GetMapping("/summary/agent")
    public ResponseEntity<com.aisupport.ticket.dto.response.TicketDashboardSummaryResponse> getAgentSummary(
        @RequestHeader(value = "X-User-Email", required = true) String userEmail) {
        
        log.info("Management requested dashboard summary for agent: {}", userEmail);
        
        return ResponseEntity.ok(
                ticketService.getAgentDashboardSummary(userEmail)
        );
    }
}
