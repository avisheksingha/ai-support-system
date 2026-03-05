package com.aisupport.analysis.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.aisupport.analysis.event.TicketCreatedEvent;
import com.aisupport.analysis.service.AnalysisProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketCreatedConsumer {

    private final AnalysisProcessingService processingService;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(topics = "ticket-created", groupId = "ai-analysis-group")
    public void consume(String payload) {

        try {
            TicketCreatedEvent event =
                    objectMapper.readValue(payload, TicketCreatedEvent.class);

            log.info("Consumed ticket-created for ticketId={}", event.getTicketId());

            processingService.processTicket(event);

        } catch (Exception ex) {
        	log.error("Failed to deserialize/process event. Payload={}", payload, ex);
            throw new RuntimeException(ex); // allow retry
        }
    }
}