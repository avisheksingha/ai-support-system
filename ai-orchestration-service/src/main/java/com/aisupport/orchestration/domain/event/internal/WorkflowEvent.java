package com.aisupport.orchestration.domain.event.internal;

import java.time.Instant;

public interface WorkflowEvent {
    String getExecutionId();
    Instant getTimestamp();
}
