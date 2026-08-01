package com.aisupport.orchestration.domain.event;

import java.time.Instant;

public interface WorkflowEvent {
    String getExecutionId();
    Instant getTimestamp();
}
