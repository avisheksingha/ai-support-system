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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses(value = {
    		@ApiResponse(responseCode = "201", description = "Ticket created successfully",
    				content = @io.swagger.v3.oas.annotations.media.Content(
    						mediaType = "application/json",
    						schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TicketResponse.class)
    				)),
    		@ApiResponse(responseCode = "400", description = "Invalid request data"),
    		@ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Tag(name = "Ticket Management", description = "Endpoints for managing support tickets")
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
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Ticket retrieved successfully",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TicketResponse.class)
        )
    ),
    @ApiResponse(responseCode = "404", description = "Ticket not found",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.aisupport.common.exception.ErrorResponse.class)
        )
    ),
    @ApiResponse(responseCode = "500", description = "Internal server error",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.aisupport.common.exception.ErrorResponse.class)
        )
    )
})
    @Tag(name = "Ticket Management", description = "Endpoints for managing support tickets")
    @GetMapping("/{ticketNumber}")
    public ResponseEntity<TicketResponse> getTicket(
            @PathVariable
            @io.swagger.v3.oas.annotations.Parameter(
            		description = "Unique ticket number",
            		required = true
            ) String ticketNumber) {
        TicketResponse response = ticketService.getTicketByNumber(ticketNumber);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
			summary = "Get all tickets",
			description = "Retrieves a list of all support tickets, with optional filtering by status"
	)
    @ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Tickets retrieved successfully"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Tag(name = "Ticket Management", description = "Endpoints for managing support tickets")
    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets(
            @RequestParam(required = false)
            @io.swagger.v3.oas.annotations.Parameter(
					description = "Filter tickets by status (e.g., NEW, ANALYZING, ANALYZED, ASSIGNED, RESOLVED, CLOSED)",
					required = false
			) String status) {
        List<TicketResponse> responses = status != null
                ? ticketService.getTicketsByStatus(status)
                : ticketService.getAllTickets();
        return ResponseEntity.ok(responses);
    }
    
    @Operation(
			summary = "Update ticket status",
			description = "Updates the status of a support ticket (e.g., NEW, ANALYZING, ANALYZED, ASSIGNED, RESOLVED, CLOSED)"
	)
    @ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Ticket status updated successfully",
					content = @io.swagger.v3.oas.annotations.media.Content(
							mediaType = "application/json",
							schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TicketResponse.class)
					)),
			@ApiResponse(responseCode = "400", description = "Invalid status value"),
			@ApiResponse(responseCode = "404", description = "Ticket not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Tag(name = "Ticket Management", description = "Endpoints for managing support tickets")
    @PatchMapping("/{ticketNumber}/status")
    public ResponseEntity<TicketResponse> updateStatus(
            @PathVariable
            @io.swagger.v3.oas.annotations.Parameter(
					description = "Unique ticket number",
					required = true
			) String ticketNumber,
            @RequestParam
            @io.swagger.v3.oas.annotations.Parameter(
					description = "New status for the ticket (e.g., NEW, ANALYZING, ANALYZED, ASSIGNED, RESOLVED, CLOSED)",
					required = true
			) String status) {
        TicketResponse response = ticketService.updateTicketStatus(ticketNumber, status);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
			summary = "Assign ticket to support agent",
			description = "Assigns a support ticket to a specific agent and updates the ticket status to ASSIGNED"
	)
    @ApiResponses(value = {
    		@ApiResponse(responseCode = "200", description = "Ticket assigned successfully",
			content = @io.swagger.v3.oas.annotations.media.Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TicketResponse.class)
			)),
			@ApiResponse(responseCode = "400", description = "Invalid agent identifier"),
			@ApiResponse(responseCode = "404", description = "Ticket not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Tag(name = "Ticket Management", description = "Endpoints for managing support tickets")
    @PatchMapping("/{ticketNumber}/assign")
    public ResponseEntity<TicketResponse> assignTicket(
            @PathVariable String ticketNumber,
            @RequestParam String assignedTo) {
        TicketResponse response = ticketService.assignTicket(ticketNumber, assignedTo);
        return ResponseEntity.ok(response);
    }
}
