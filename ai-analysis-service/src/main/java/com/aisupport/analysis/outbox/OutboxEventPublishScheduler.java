package com.aisupport.analysis.outbox;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.common.constant.Correlation;
import com.aisupport.common.constant.HttpHeaders;
import com.aisupport.common.constant.KafkaTopics;
import com.aisupport.common.event.TicketAnalyzedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublishScheduler {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPendingEvents() {

        List<OutboxEvent> events = repository.findTop50ByStatusOrderByCreatedAtAsc(
        		OutboxEvent.Status.PENDING
        );
        
        if (events.isEmpty()) {
            return;
        }
        
        log.debug("Publishing {} pending outbox events", events.size());
        
        for (OutboxEvent event : events) {
            processEvent(event);
        }
    }

    private void processEvent(OutboxEvent event) {        	
        try {
        	
        	String topic = mapTopic(event.getEventType());
        	Object payloadObject = deserializePayload(event.getPayload(), event.getEventType());
            
            ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(topic, null,
            		event.getAggregateId(), payloadObject);

            String correlationId = MDC.get(Correlation.MDC_KEY);
            if (correlationId != null) {
            	producerRecord.headers().add(HttpHeaders.CORRELATION_ID,
                        correlationId.getBytes(StandardCharsets.UTF_8));
            }
            
            kafkaTemplate.send(producerRecord).get(5, TimeUnit.SECONDS);
        	
            /* kafkaTemplate.send(topic, event.getAggregateId(), payloadObject)
            	.get(5, TimeUnit.SECONDS); // IMPORTANT: timeout prevents indefinite blocking */

            event.setStatus(OutboxEvent.Status.SENT);
            event.setProcessedAt(LocalDateTime.now());

            log.info("Published outbox event {}", event.getId());

        } catch (InterruptedException e) {
        	
			Thread.currentThread().interrupt(); // IMPORTANT: restore interrupt status	
			
			log.error("Interrupted while publishing outbox event {}", event.getId(), e);			
			markFailed(event);
			
		} catch (TimeoutException e) {
			
		    log.error("Timeout publishing outbox event {}", event.getId(), e);		    
		    markFailed(event);
		    
		} catch (Exception e) {
			
            log.error("Failed to publish outbox event {}", event.getId(), e);
            markFailed(event);
        }
        
    }
    
    private void markFailed(OutboxEvent event) {
        event.setRetryCount(event.getRetryCount() + 1);
        event.setProcessedAt(LocalDateTime.now());

        if (event.getRetryCount() >= OutboxEvent.MAX_RETRIES) {
            event.setStatus(OutboxEvent.Status.DEAD);
            log.error("Outbox event {} permanently failed after {} retries",
                    event.getId(), OutboxEvent.MAX_RETRIES);
        } else {
            event.setStatus(OutboxEvent.Status.FAILED);
            log.warn("Outbox event {} failed, retry {}/{}",
                    event.getId(), event.getRetryCount(), OutboxEvent.MAX_RETRIES);
        }
    }
    
    private Object deserializePayload(String payload, String eventType) throws Exception {
        Class<?> clazz = switch (eventType) {
            case "TicketAnalyzedEvent" -> TicketAnalyzedEvent.class;
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
        return objectMapper.readValue(payload, clazz);
    }
    
    private String mapTopic(String eventType) {

    	return switch (eventType) {
			case "TicketAnalyzedEvent" -> KafkaTopics.TICKET_ANALYZED;
			default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
		};
	}

}
