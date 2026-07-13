package com.aisupport.orchestration.application.operations.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkflowSummaryDTO {
    private String workflowId;
    private Long ticketId;
    private String correlationId;
    private String definitionId;
    private String state;
    private Instant startedAt;
    private Instant completedAt;
    private Long durationMs;
}
