package com.aisupport.ticket.outbox;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishEvents() {

        List<OutboxEvent> events =
                repository.findTop100ByStatusOrderByCreatedAtAsc("NEW");

        for (OutboxEvent event : events) {

            try {
                String topic = mapTopic(event.getEventType());

                kafkaTemplate.send(
                        topic,
                        event.getAggregateId(),
                        event.getPayload()
                );

                event.setStatus("SENT");
                event.setProcessedAt(LocalDateTime.now());

            } catch (Exception e) {
                log.error("Failed to publish outbox event {}", event.getId(), e);
                event.setStatus("FAILED");
            }
        }
    }

    private String mapTopic(String eventType) {

        return switch (eventType) {
            case "TicketCreatedEvent" -> "ticket-created";
            default -> throw new IllegalArgumentException(
                    "Unknown event type: " + eventType);
        };
    }
}