package com.aisupport.ticket.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.aisupport.ticket.event.TicketRoutedEvent;
import com.aisupport.ticket.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketRoutedConsumer {

    private final TicketService ticketService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "ticket-routed", groupId = "ticket-group")
    public void consume(String payload) {

        try {
        	
        	// Step 1 — unwrap double-encoded JSON            
            String cleanJson = objectMapper.readValue(payload, String.class);

            // Step 2 — map to object
            TicketRoutedEvent event =
                    objectMapper.readValue(cleanJson, TicketRoutedEvent.class);

            log.info("Received routed event ticketId={} team={} priority={}",
                    event.getTicketId(),
                    event.getAssignToTeam(),
                    event.getPriority());

            ticketService.applyRoutingResult(event);

        } catch (Exception e) {
            log.error("Failed to process routed payload={}", payload, e);
        }
    }
}
