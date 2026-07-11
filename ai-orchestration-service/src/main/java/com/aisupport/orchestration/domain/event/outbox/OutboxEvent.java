package com.aisupport.orchestration.domain.event.outbox;

import java.time.Instant;

public interface OutboxEvent {
    String getEventId();
    String getEventType();
    String getPayload();
    Instant getCreatedAt();
}
