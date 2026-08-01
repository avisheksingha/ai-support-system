package com.aisupport.orchestration.application.workflow;

import java.time.Duration;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkflowExecutionResult {
    private final String workflowId;
    private final String executionId;
    private final WorkflowStatus status;
    private final Duration duration;
    private final int stepsExecuted;
    private final String error;
}
