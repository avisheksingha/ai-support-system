package com.aisupport.orchestration.infrastructure.web;

import java.time.Instant;
import java.util.Collections;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.orchestration.application.operations.MetricsQueryService;
import com.aisupport.orchestration.application.operations.dto.OperationsDashboardResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/orchestration/operations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Operations Dashboard", description = "Endpoints for the operations observability platform")
public class OperationsDashboardController {

    private final MetricsQueryService metricsQueryService;

    @GetMapping("/overview")
    @Operation(summary = "Get operations dashboard overview", description = "Returns aggregated AI orchestration metrics and recent executions")
    public ResponseEntity<OperationsDashboardResponse> getDashboardOverview(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) String workflowType,
            @RequestParam(required = false) String outcome,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String provider) {
        
        log.info("Fetching operations dashboard metrics");
        
        OperationsDashboardResponse response = OperationsDashboardResponse.builder()
                .overview(metricsQueryService.getOverviewMetrics(from, to))
                .recentExecutions(Collections.emptyList()) // Simple for V1, we'd fetch top 10 from repo
                .build();
                
        return ResponseEntity.ok(response);
    }
}
