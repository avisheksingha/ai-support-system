package com.aisupport.orchestration.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.orchestration.application.customer.CustomerDashboardService;
import com.aisupport.orchestration.application.customer.dto.CustomerDashboardResponse;
import com.aisupport.orchestration.application.customer.dto.CustomerTicketDetailResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/orchestration/dashboard/customer")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Dashboard", description = "Aggregated customer dashboard API acting as BFF")
public class CustomerDashboardController {

    private final CustomerDashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Get Customer Dashboard", description = "Returns aggregated dashboard for the authenticated customer")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved customer dashboard summary")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerDashboardResponse> getCustomerDashboard(
            @RequestHeader(value = "X-User-Email", defaultValue = "customer@example.com") String userEmail,
            @RequestHeader(value = "X-User-Name", defaultValue = "Customer") String userName) {

        log.info("Fetching dashboard for customer: {}", userEmail);

        return ResponseEntity.ok(dashboardService.getCustomerDashboard(userEmail, userName));
    }

    @GetMapping("/tickets/{ticketNumber}")
    @Operation(summary = "Get Customer Ticket Detail", description = "Returns aggregated ticket details and messages for the customer")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved ticket details")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerTicketDetailResponse> getCustomerTicketDetail(
            @PathVariable String ticketNumber,
            @RequestHeader(value = "X-User-Email", defaultValue = "customer@example.com") String userEmail) {

        log.info("Fetching ticket details for customer: {} on ticket: {}", userEmail, ticketNumber);

        return ResponseEntity.ok(dashboardService.getCustomerTicketDetail(userEmail, ticketNumber));
    }
}
