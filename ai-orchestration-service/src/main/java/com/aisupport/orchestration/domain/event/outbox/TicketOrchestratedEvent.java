package com.aisupport.orchestration.domain.event.outbox;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketOrchestratedEvent implements OutboxEvent {
    
    @Builder.Default
    private final String eventId = UUID.randomUUID().toString();
    
    private final Long ticketId;
    private final String status;
    private final String payload;
    
    @Builder.Default
    private final Instant createdAt = Instant.now();
    
    @Override
    public String getEventType() {
        return "TicketOrchestratedEvent";
    }
}
