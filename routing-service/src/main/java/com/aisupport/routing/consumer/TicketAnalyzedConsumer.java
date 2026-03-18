package com.aisupport.routing.consumer;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.aisupport.common.constant.Correlation;
import com.aisupport.common.constant.HttpHeaders;
import com.aisupport.common.constant.KafkaGroups;
import com.aisupport.common.constant.KafkaTopics;
import com.aisupport.common.event.TicketAnalyzedEvent;
import com.aisupport.common.exception.TicketEventProcessingException;
import com.aisupport.routing.service.RoutingService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketAnalyzedConsumer {
	
	private final RoutingService routingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.TICKET_ANALYZED, groupId = KafkaGroups.ROUTING)
    public void consume(ConsumerRecord<String, String> consumerRecord){
    	
    	// Extract correlationId from Kafka header into MDC
    	Header correlationHeader = consumerRecord.headers().lastHeader(HttpHeaders.CORRELATION_ID);
        if (correlationHeader != null) {
            MDC.put(Correlation.MDC_KEY, new String(correlationHeader.value(), StandardCharsets.UTF_8));
        }

        try {
        	
        	String payload = consumerRecord.value(); // extract payload
        	
        	TicketAnalyzedEvent event = objectMapper.readValue(payload, TicketAnalyzedEvent.class);
        	
            log.info("Consumed ticket-analyzed event: ticketId={} intent={} urgency={}",
                    event.getTicketId(),
                    event.getIntent(),
                    event.getUrgency());

            routingService.route(event);

        } catch (Exception ex) {        	
            log.error("Failed to process ticket-analyzed event key={}", consumerRecord.key(), ex);            
            throw new TicketEventProcessingException("Error processing ticket-analyzed event", ex);
        } finally {
        	MDC.remove(Correlation.MDC_KEY);
        }
    }
}
