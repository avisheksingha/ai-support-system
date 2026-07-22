package com.aisupport.orchestration.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.orchestration.application.agent.AgentDashboardService;
import com.aisupport.orchestration.application.agent.dto.AgentDashboardResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/orchestration/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Agent Dashboard", description = "Aggregated agent dashboard API")
public class AgentDashboardController {

    private final AgentDashboardService dashboardService;

    @GetMapping("/agent")
    @Operation(summary = "Get Agent Dashboard", description = "Returns aggregated dashboard for the authenticated agent")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved agent dashboard summary")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<AgentDashboardResponse> getAgentDashboard() {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication != null ? authentication.getName() : "agent@example.com";
        // Attempt to extract friendly name if available, otherwise fallback
        String userName = userEmail;

        log.info("Fetching dashboard for agent: {}", userEmail);

        return ResponseEntity.ok(dashboardService.getAgentDashboard(userEmail, userName));
    }
}
