package com.aisupport.common.event;

import java.time.Instant;

import com.aisupport.common.enums.WorkflowOutcome;

public record EventMetadata(
    String eventId,
    int eventVersion,
    String correlationId,
    String workflowExecutionId,
    String workflowVersion,
    String promptVersion,
    String modelProfile,
    String orchestratorVersion,
    Long processingDurationMs,
    WorkflowOutcome outcome,
    Instant generatedAt
) {}
