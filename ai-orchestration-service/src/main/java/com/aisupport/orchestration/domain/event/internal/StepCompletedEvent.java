package com.aisupport.orchestration.domain.event.internal;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StepCompletedEvent implements WorkflowEvent {
    private final String executionId;
    private final String workflowId;
    private final String stepName;
    private final boolean success;
    
    @Builder.Default
    private final Instant timestamp = Instant.now();
}
