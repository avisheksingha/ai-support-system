package com.aisupport.ticket.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.ticket.dto.TicketRequest;
import com.aisupport.ticket.dto.TicketResponse;
import com.aisupport.ticket.service.TicketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/tickets")
@CrossOrigin(origins = "http://localhost:8082")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tickets", description = "Support ticket management endpoints")
public class TicketController {
	
private final TicketService ticketService;    
    
    @Operation(
    		summary = "Create a new support ticket",
            description = "Creates a new support ticket and automatically analyzes it using AI"
    )
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
            		description = "Ticket details",
            		required = true
            ) TicketRequest request) {
        log.info("Received request to create ticket from: {}", request.getCustomerEmail());
        
        TicketResponse response = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(
    		summary = "Get ticket by number",
    		description = "Retrieves a support ticket by its unique ticket number"
    )
    @GetMapping("/{ticketNumber}")
    public ResponseEntity<TicketResponse> getTicket(
            @PathVariable
            @io.swagger.v3.oas.annotations.Parameter(
            		description = "Unique ticket number",
            		required = true
            ) String ticketNumber) {
    	
    	return ResponseEntity.ok(
                ticketService.getTicketByNumber(ticketNumber)
        );
    }

    @Operation(
    		summary = "Get ticket by ID",
    		description = "Retrieves a support ticket by its database ID"
    )
    @GetMapping("/id/{id}")
    public ResponseEntity<TicketResponse> getTicketById(
    		@PathVariable
    		@io.swagger.v3.oas.annotations.Parameter(
					description = "Database ID of the ticket",
					required = true
			)
    		Long id) {
    	
    	return ResponseEntity.ok(
                ticketService.getTicketById(id)
        );
    }
    
    @Operation(
			summary = "Get all tickets",
			description = "Retrieves a list of all support tickets, with optional filtering by status"
	)
    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets(
            @RequestParam(required = false)
            @io.swagger.v3.oas.annotations.Parameter(
					description = "Filter tickets by status (e.g., NEW, ANALYZING, ANALYZED, ASSIGNED, RESOLVED, CLOSED)",
					required = false
			) String status) {
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
    @PatchMapping("/{ticketNumber}/status")
    public ResponseEntity<TicketResponse> updateStatus(
            @PathVariable
            @io.swagger.v3.oas.annotations.Parameter(
					description = "Unique ticket number",
					required = true
			) String ticketNumber,
            @RequestParam
            @io.swagger.v3.oas.annotations.Parameter(
					description = "New status for the ticket",
					required = true
			) String status,
            @RequestParam(required = false) Integer slaHours) {
    	
    	return ResponseEntity.ok(
                ticketService.updateTicketStatus(ticketNumber, status)
        );
    }
    
    @Operation(
			summary = "Assign ticket to support agent",
			description = "Assigns a support ticket to a specific agent and updates the ticket status to ASSIGNED"
	)
    @PatchMapping("/{ticketNumber}/assign")
    public ResponseEntity<TicketResponse> assignTicket(
            @PathVariable String ticketNumber,
            @RequestParam String assignedTo,
            @RequestParam(required = false) Integer slaHours) {
    	
    	return ResponseEntity.ok(
                ticketService.assignTicket(ticketNumber, assignedTo, slaHours)
        );
    }

    @Operation(
			summary = "Update ticket priority",
			description = "Updates the priority level of a support ticket"
	)
    @PatchMapping("/{ticketNumber}/priority")
    public ResponseEntity<TicketResponse> updatePriority(
            @PathVariable String ticketNumber,
            @RequestParam String priority,
            @RequestParam(required = false) Integer slaHours) {
    	
    	return ResponseEntity.ok(
                ticketService.updateTicketPriority(ticketNumber, priority, slaHours)
        );
    }
}
