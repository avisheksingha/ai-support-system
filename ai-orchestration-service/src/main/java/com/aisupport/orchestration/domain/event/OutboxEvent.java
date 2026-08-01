package com.aisupport.orchestration.domain.event;

import java.time.Instant;

public interface OutboxEvent {
    String getEventId();
    String getEventType();
    String getPayload();
    Instant getCreatedAt();
}
