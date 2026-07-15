package com.aisupport.common.event;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainEvent<T> {
    private String eventId;
    private EventType eventType;
    private String entityType;
    private String entityId;
    private String correlationId;
    private String sourceService;
    private Instant timestamp;
    private T payload;
}
