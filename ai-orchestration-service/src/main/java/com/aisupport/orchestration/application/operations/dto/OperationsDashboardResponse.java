package com.aisupport.orchestration.application.operations.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Response containing operations dashboard overview and recent executions")
public class OperationsDashboardResponse {
    @Schema(description = "Aggregated operations metrics overview")
    private OperationsOverviewDTO overview;
    
    @Schema(description = "List of recent workflow executions")
    private List<WorkflowSummaryDTO> recentExecutions;
}
