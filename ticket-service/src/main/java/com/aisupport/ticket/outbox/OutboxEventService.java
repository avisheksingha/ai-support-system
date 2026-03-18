package com.aisupport.ticket.outbox;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.common.exception.OutboxEventException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional // atomic with calling transaction
    public void publishEvent(String aggregateType,
                             String aggregateId,
                             String eventType,
                             Object payload) {

        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);

            OutboxEvent event = OutboxEvent.builder()
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(jsonPayload)
                    .status(OutboxEvent.Status.PENDING) // status + retryCount set by @PrePersist/@Builder.Default
                    .build();

            repository.save(event);

        } catch (Exception e) {
            throw new OutboxEventException("Failed to serialize outbox event", e);
        }
    }

}
