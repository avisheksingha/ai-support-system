package com.aisupport.ticket.customer.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.ticket.dto.TicketRequest;
import com.aisupport.ticket.dto.TicketResponse;
import com.aisupport.ticket.service.TicketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api/v1/tickets", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Tickets", description = "Customer self-service ticket endpoints")
public class CustomerTicketController {
    
    private final TicketService ticketService;
    
    @Operation(
        summary = "Create a new support ticket",
        description = "Creates a new support ticket for the authenticated customer"
    )
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ResponseEntity<TicketResponse> createCustomerTicket(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Email", required = true) String userEmail,
            @Valid @RequestBody TicketRequest request) {
        TicketResponse response = ticketService.createTicket(userId, userEmail, request);
        log.info("Customer [{}] created ticket [{}]", userEmail, response.getTicketNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(
        summary = "Get my tickets",
        description = "Retrieves tickets created by the authenticated customer, ordered by newest first"
    )
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my")
    public ResponseEntity<List<TicketResponse>> getMyTickets(
            @RequestHeader(value = "X-User-Email", required = true) String userEmail) {
        log.info("Customer [{}] requested their tickets list", userEmail);
        return ResponseEntity.ok(ticketService.getTicketsByCustomerEmail(userEmail));
    }
    
    @Operation(
        summary = "Get my ticket by number",
        description = "Retrieves a specific ticket if owned by the authenticated customer"
    )
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my/{ticketNumber}")
    public ResponseEntity<TicketResponse> getMyTicket(
            @Parameter(description = "Unique ticket number") @PathVariable String ticketNumber,
            @RequestHeader(value = "X-User-Email", required = true) String userEmail) {
        log.info("Customer [{}] requested ticket [{}]", userEmail, ticketNumber);
        return ResponseEntity.ok(ticketService.getCustomerTicketByNumber(ticketNumber, userEmail));
    }
}
