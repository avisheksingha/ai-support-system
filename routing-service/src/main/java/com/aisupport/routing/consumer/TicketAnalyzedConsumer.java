package com.aisupport.routing.consumer;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import com.aisupport.common.constants.Correlation;
import com.aisupport.common.constants.HttpHeaders;
import com.aisupport.common.event.TicketAnalyzedEvent;
import com.aisupport.common.exception.TicketEventProcessingException;
import com.aisupport.routing.service.RoutingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketAnalyzedConsumer {
	
	private final RoutingService routingService;

    // @KafkaListener(topics = KafkaTopics.TICKET_ANALYZED, groupId = KafkaGroups.ROUTING)
    public void consume(ConsumerRecord<String, TicketAnalyzedEvent> consumerRecord){
    	
    	// Extract correlationId from Kafka header into MDC
    	Header correlationHeader = consumerRecord.headers().lastHeader(HttpHeaders.CORRELATION_ID);
        if (correlationHeader != null) {
            MDC.put(Correlation.MDC_KEY, new String(correlationHeader.value(), StandardCharsets.UTF_8));
        }

        try {
        	TicketAnalyzedEvent event = consumerRecord.value();
        	
            log.info("Consumed ticket-analyzed event: ticketId={} intent={} urgency={}",
                    event.getTicketId(),
                    event.getAnalysis().intent(),
                    event.getAnalysis().urgency());

            routingService.route(event);

        } catch (Exception ex) {        	
            log.error("Failed to process ticket-analyzed event key={}", consumerRecord.key(), ex);            
            throw new TicketEventProcessingException("Error processing ticket-analyzed event", ex);
        } finally {
        	MDC.remove(Correlation.MDC_KEY);
        }
    }
}
