package com.aisupport.orchestration.infrastructure.web;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.orchestration.application.operations.MetricsQueryService;
import com.aisupport.orchestration.application.operations.dto.OperationsDashboardResponse;
import com.aisupport.orchestration.application.operations.dto.WorkflowSummaryDTO;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowExecutionEntity;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowExecutionRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/orchestration/operations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Operations Dashboard", description = "Endpoints for the operations observability platform")
public class OperationsController {

    private final MetricsQueryService metricsQueryService;
    private final WorkflowExecutionRepository workflowExecutionRepository;

    @GetMapping("/overview")
    @Operation(summary = "Get operations dashboard overview", description = "Returns aggregated AI orchestration metrics and recent executions")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved operations overview")
    public ResponseEntity<OperationsDashboardResponse> getDashboardOverview(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) String workflowType,
            @RequestParam(required = false) String outcome,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String provider) {
        
        log.info("Fetching operations dashboard metrics");
        
        List<WorkflowExecutionEntity> executions = workflowExecutionRepository.findAll();
        List<WorkflowSummaryDTO> recentExecutions = executions.stream()
                .sorted(Comparator.comparing(WorkflowExecutionEntity::getCreatedAt).reversed())
                .limit(10)
                .map(this::toWorkflowSummaryDTO)
                .collect(Collectors.toList());
        
        OperationsDashboardResponse response = OperationsDashboardResponse.builder()
                .overview(metricsQueryService.getOverviewMetrics(from, to))
                .recentExecutions(recentExecutions)
                .build();
                
        return ResponseEntity.ok(response);
    }
    
    private WorkflowSummaryDTO toWorkflowSummaryDTO(WorkflowExecutionEntity entity) {
        long duration = 0;
        if (entity.getCreatedAt() != null && entity.getCompletedAt() != null) {
            duration = entity.getCompletedAt().toEpochMilli() - entity.getCreatedAt().toEpochMilli();
        }
        
        String ticketNum = null;
        if (entity.getAttributes() != null && entity.getAttributes().get("ticketNumber") != null) {
            ticketNum = entity.getAttributes().get("ticketNumber").toString();
        } else if (entity.getTicketId() != null) {
            ticketNum = "TKT-" + entity.getTicketId();
        }
        
        return WorkflowSummaryDTO.builder()
                .workflowId(entity.getId())
                .definitionId(entity.getDefinitionId())
                .correlationId(entity.getCorrelationId())
                .ticketId(entity.getTicketId())
                .ticketNumber(ticketNum)
                .state(entity.getState() != null ? entity.getState().name() : "UNKNOWN")
                .startedAt(entity.getCreatedAt())
                .completedAt(entity.getCompletedAt())
                .durationMs(duration)
                .build();
    }
}
