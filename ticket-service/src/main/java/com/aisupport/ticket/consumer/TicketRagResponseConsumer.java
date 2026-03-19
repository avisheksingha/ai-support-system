package com.aisupport.ticket.consumer;

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
import com.aisupport.common.event.TicketRagResponseEvent;
import com.aisupport.common.exception.TicketEventProcessingException;
import com.aisupport.ticket.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketRagResponseConsumer {

    private final TicketService ticketService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.TICKET_RAG_RESPONSE, groupId = KafkaGroups.TICKET)
    public void consume(ConsumerRecord<String, String> consumerRecord) {

    	// Extract correlationId from Kafka header into MDC
        Header correlationHeader = consumerRecord.headers()
                .lastHeader(HttpHeaders.CORRELATION_ID);
        if (correlationHeader != null) {
            MDC.put(Correlation.MDC_KEY,
                    new String(correlationHeader.value(), StandardCharsets.UTF_8));
        }

        try {
            String payload = consumerRecord.value();

            TicketRagResponseEvent event = objectMapper.readValue(
                    payload, TicketRagResponseEvent.class);

            log.info("Consumed rag-response event: ticketId={}", event.getTicketId());

            ticketService.applyRagResponse(event);

        } catch (Exception ex) {
            log.error("Failed to process rag-response event key={}",
                    consumerRecord.key(), ex);
            throw new TicketEventProcessingException(
                    "Error processing rag-response event", ex);
        } finally {
            MDC.remove(Correlation.MDC_KEY);
        }
    }
}