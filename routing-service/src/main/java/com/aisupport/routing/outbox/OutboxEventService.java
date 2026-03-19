package com.aisupport.routing.outbox;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.common.constant.Correlation;
import com.aisupport.common.exception.OutboxEventException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void publishEvent(
    		String aggregateType,
    		String aggregateId,
            String eventType,
            Object payloadObject
    ) {
        try {
            String payload = objectMapper.writeValueAsString(payloadObject);

            OutboxEvent event = OutboxEvent.builder()
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(payload)
                    .correlationId(MDC.get(Correlation.MDC_KEY)) // capture from HTTP thread
                    .build();

            repository.save(event);

        } catch (Exception e) {
            throw new OutboxEventException("Failed to serialize outbox event", e);
        }
    }
}