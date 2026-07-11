package com.aisupport.orchestration.infrastructure.messaging;

import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.orchestration.infrastructure.persistence.entity.OutboxEventEntity;
import com.aisupport.orchestration.infrastructure.persistence.repository.OutboxEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelayString = "${governance.outbox.poll-rate:5000}")
    @Transactional
    public void publishEvents() {
        List<OutboxEventEntity> events = repository.findAll(); // In real prod, fetch limited batch and order by createdAt
        if (events.isEmpty()) {
            return;
        }

        log.debug("Found {} outbox events to publish", events.size());
        
        for (OutboxEventEntity event : events) {
            try {
                // Determine topic based on aggregate or event type. Hardcoded for V1 portfolio.
                String topic = "ticket-" + event.getEventType().replace("Ticket", "").toLowerCase();
                if (topic.endsWith("event")) {
                    topic = topic.substring(0, topic.length() - 5);
                }
                
                // Assuming payload is a JSON string of the event
                kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload()).get();
                
                // If successful, remove from outbox
                repository.delete(event);
                log.info("Published outbox event {} for aggregate {}", event.getEventType(), event.getAggregateId());
                
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                log.error("Failed to publish outbox event {}, will retry next cycle", event.getId(), e);
                // We don't throw to avoid rolling back successfully published events in this batch
            }
        }
    }
}
