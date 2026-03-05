package com.aisupport.ticket.outbox;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

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
                    .status(OutboxEvent.Status.NEW)
                    .build();

            repository.save(event);

        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize outbox event", e);
        }
    }

}
