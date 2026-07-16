package com.aisupport.ticket.consumer;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.aisupport.common.constant.Correlation;
import com.aisupport.common.constant.HttpHeaders;
import com.aisupport.common.constant.KafkaGroups;
import com.aisupport.common.constant.KafkaTopics;
import com.aisupport.common.event.TicketRoutedEvent;
import com.aisupport.common.exception.TicketEventProcessingException;
import com.aisupport.ticket.service.TicketService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "ticket.legacy.consumers.enabled", havingValue = "true", matchIfMissing = false)
public class TicketRoutedConsumer {

    private final TicketService ticketService;

    @KafkaListener(topics = KafkaTopics.TICKET_ROUTED, groupId = KafkaGroups.TICKET)
    public void consume(ConsumerRecord<String, TicketRoutedEvent> consumerRecord) {
    	
    	// Extract correlationId from Kafka header into MDC
    	Header correlationHeader = consumerRecord.headers().lastHeader(HttpHeaders.CORRELATION_ID);
        if (correlationHeader != null) {
            MDC.put(Correlation.MDC_KEY, new String(correlationHeader.value(), StandardCharsets.UTF_8));
        }

        try {
        	TicketRoutedEvent event = consumerRecord.value();

            log.info("Consumed ticket-routed event: ticketId={} team={} priority={}",
                    event.getTicketId(),
                    event.getAssignToTeam(),
                    event.getPriority());

            ticketService.applyRoutingResult(event);

        } catch (Exception ex) {        	
            log.error("Failed to process ticket-routed event key={}", consumerRecord.key(), ex);            
            throw new TicketEventProcessingException("Error processing ticket-routed event", ex);
        } finally {
        	MDC.remove(Correlation.MDC_KEY);
        }
    }
}
