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
                repository.findTop50ByStatusOrderByCreatedAtAsc(
                		OutboxEvent.Status.NEW
				);
        
        if (events.isEmpty()) {
            return;
        }

        log.debug("Publishing {} new outbox events", events.size());

        for (OutboxEvent event : events) {

            try {
                String topic = mapTopic(event.getEventType());

                kafkaTemplate.send(
                        topic,
                        event.getAggregateId(),
                        event.getPayload()
                ).get(); // IMPORTANT → ensures Kafka ack before marking SENT

                event.setStatus(OutboxEvent.Status.SENT);
                event.setProcessedAt(LocalDateTime.now());
                
                log.info("Published outbox event {} to topic {}", event.getId(), topic);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();  // IMPORTANT

                log.error("Interrupted while publishing outbox event {}", event.getId(), e);

                event.setStatus(OutboxEvent.Status.FAILED);
                event.setProcessedAt(LocalDateTime.now());

            } catch (Exception e) {
            	
                log.error("Failed to publish outbox event {}", event.getId(), e);
                
                event.setStatus(OutboxEvent.Status.FAILED);
                event.setProcessedAt(LocalDateTime.now());
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