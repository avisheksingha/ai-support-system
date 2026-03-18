package com.aisupport.analysis.outbox;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisherScheduler {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                repository.findTop50ByStatusOrderByCreatedAtAsc(
                        OutboxEvent.Status.PENDING
                );
        
        if (events.isEmpty()) {
            return;
        }
        
        log.debug("Publishing {} pending outbox events", events.size());

        for (OutboxEvent event : events) {
        	
            try {
            	
            	String topic = mapTopic(event.getEventType());
            	Object payloadObject = objectMapper.readValue(event.getPayload(), Object.class);
            	
                kafkaTemplate.send(
                        topic,
                        event.getAggregateId(),
                        payloadObject
                ).get(); // synchronous to ensure delivery

                event.setStatus(OutboxEvent.Status.SENT);
                event.setProcessedAt(LocalDateTime.now());

                log.info("Published outbox event {}", event.getId());

            } catch (InterruptedException e) {
            	
				Thread.currentThread().interrupt(); // IMPORTANT: restore interrupt status
				
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
			case "TicketAnalyzedEvent" -> "ticket-analyzed";
			default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
		};
	}

}
