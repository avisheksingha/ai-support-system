package com.aisupport.orchestration.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.orchestration.application.admin.AdminDashboardService;
import com.aisupport.orchestration.application.admin.dto.AdminDashboardResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/orchestration/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Dashboard", description = "Aggregated admin dashboard API")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/admin")
    @Operation(summary = "Get Admin Dashboard", description = "Returns aggregated dashboard for the authenticated admin")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved admin dashboard summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardResponse> getAdminDashboard(
            @RequestHeader(value = "X-User-Email", defaultValue = "admin@example.com") String userEmail) {
        
        log.info("Fetching dashboard for admin: {}", userEmail);

        return ResponseEntity.ok(dashboardService.getDashboard(userEmail));
    }
}
