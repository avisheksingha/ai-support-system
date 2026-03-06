package com.aisupport.routing.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.aisupport.routing.event.TicketAnalyzedEvent;
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

    @KafkaListener(topics = "ticket-analyzed", groupId = "routing-group")
    public void consume(String payload){

        try {
        	
        	TicketAnalyzedEvent event = objectMapper.readValue(payload, TicketAnalyzedEvent.class);
        	
            log.info("Received analyzed ticket: ticketId={} intent={} urgency={}",
                    event.getTicketId(),
                    event.getIntent(),
                    event.getUrgency());

            routingService.route(event);

        } catch (Exception e) {
            log.error("Failed to process ticket analyzed payload={}", payload, e);
        }
    }
}
