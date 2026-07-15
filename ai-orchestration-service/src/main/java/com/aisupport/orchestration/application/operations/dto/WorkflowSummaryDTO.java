package com.aisupport.orchestration.application.operations.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Summary of a workflow execution")
public class WorkflowSummaryDTO {
    @Schema(description = "Workflow definition ID", example = "analyze-workflow")
    private String workflowId;
    
    @Schema(description = "Associated ticket ID", example = "12")
    private Long ticketId;
    
    @Schema(description = "Correlation ID tracing the full request lifecycle", example = "aa941075-43ad-4c6e-9ad8-d24a009bf94e")
    private String correlationId;
    
    @Schema(description = "Workflow execution ID", example = "f206f386-d396-4e2e-8dc2-5d79b9a32e7c")
    private String definitionId;
    
    @Schema(description = "Current execution state", example = "COMPLETED")
    private String state;
    
    @Schema(description = "Execution start time")
    private Instant startedAt;
    
    @Schema(description = "Execution completion time")
    private Instant completedAt;
    
    @Schema(description = "Total execution duration in milliseconds", example = "16729")
    private Long durationMs;
}
