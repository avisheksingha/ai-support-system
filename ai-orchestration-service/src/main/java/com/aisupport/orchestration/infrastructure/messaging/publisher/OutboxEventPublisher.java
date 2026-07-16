package com.aisupport.orchestration.infrastructure.messaging.publisher;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.common.constants.Correlation;
import com.aisupport.common.constants.HttpHeaders;
import com.aisupport.common.constants.KafkaTopics;
import com.aisupport.common.enums.OutboxStatus;
import com.aisupport.common.event.EventType;
import com.aisupport.common.event.TicketOrchestratedEvent;
import com.aisupport.common.exception.OutboxEventException;
import com.aisupport.orchestration.infrastructure.persistence.entity.OutboxEventEntity;
import com.aisupport.orchestration.infrastructure.persistence.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelayString = "${governance.outbox.poll-rate:5000}")
    @Transactional
    public void publishEvents() {
        List<OutboxEventEntity> candidates = new ArrayList<>();
        candidates.addAll(repository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING));
        candidates.addAll(repository.findByStatusAndRetryCountLessThan(
        		OutboxStatus.FAILED, OutboxEventEntity.MAX_RETRIES
        ));
 
        List<OutboxEventEntity> events = candidates.stream()
                .sorted(Comparator.comparing(OutboxEventEntity::getCreatedAt))
                .limit(50)
                .toList();
 
        if (events.isEmpty()) {
            return;
        }

        log.debug("Publishing {} new outbox events", events.size());
        
        for (OutboxEventEntity event : events) {
            processEvent(event);
        }
    }
    
    private void processEvent(OutboxEventEntity event) {
    	 
        try {
 
            // Restore correlationId into MDC from stored value
            if (event.getCorrelationId() != null) {
                MDC.put(Correlation.MDC_KEY, event.getCorrelationId());
            }
 
            log.debug("Processing outbox event {} type={}", event.getId(), event.getEventType()); // after MDC restore
 
            String topic = mapTopic(event.getEventType());
            Object payloadObject = deserializePayload(event.getPayload(), event.getEventType());
 
            ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(topic, null,
                    event.getAggregateId(), payloadObject);
 
            // Propagate correlationId to Kafka header
            if (event.getCorrelationId() != null) {
                producerRecord.headers().add(HttpHeaders.CORRELATION_ID,
                        event.getCorrelationId().getBytes(StandardCharsets.UTF_8));
            }
 
            kafkaTemplate.send(producerRecord).get(5, TimeUnit.SECONDS);

            event.setStatus(OutboxStatus.SENT);
            event.setProcessedAt(Instant.now());
 
            log.info("Published outbox event {} to topic {}", event.getId(), topic);
 
        } catch (InterruptedException e) {
 
            Thread.currentThread().interrupt(); // IMPORTANT: restore interrupt status

            log.error("Interrupted while publishing outbox event {}", event.getId(), e);
            markFailed(event);
 
        } catch (TimeoutException e) {
            log.error("Timeout publishing outbox event {}", event.getId(), e);
            markFailed(event);
 
        } catch (ExecutionException | RuntimeException e) {
            log.error("Failed to publish outbox event {}", event.getId(), e);
            markFailed(event);
 
        } finally {
            MDC.remove(Correlation.MDC_KEY); // clean up scheduler thread MDC
        }
    }
    
    private void markFailed(OutboxEventEntity event) {
    	
        event.setRetryCount(event.getRetryCount() + 1);
        event.setProcessedAt(Instant.now());
        

        if (event.getRetryCount() >= OutboxEventEntity.MAX_RETRIES) {
            event.setStatus(OutboxStatus.DEAD);
            log.error("Outbox event {} permanently failed after {} retries",
                    event.getId(), OutboxEventEntity.MAX_RETRIES);
        } else {
            event.setStatus(OutboxStatus.FAILED);
            log.warn("Outbox event {} failed, retry {}/{}",
                    event.getId(), event.getRetryCount(), OutboxEventEntity.MAX_RETRIES);
        }
    }
    
    private Object deserializePayload(String payload, EventType eventType) {
        Class<?> clazz = switch (eventType) {
            case TICKET_ORCHESTRATED -> TicketOrchestratedEvent.class;
            default -> throw new OutboxEventException("Unknown event type: " + eventType);
        };
        try {
            return objectMapper.readValue(payload, clazz);
        } catch (JsonProcessingException e) {
            throw new OutboxEventException("Failed to deserialize outbox payload for eventType: " + eventType, e);
        }
    }

    private String mapTopic(EventType eventType) {

        return switch (eventType) {
            case TICKET_ORCHESTRATED -> KafkaTopics.TICKET_ORCHESTRATED;
            default -> throw new OutboxEventException("Unknown event type: " + eventType);
        };
    }
}
