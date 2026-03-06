package com.aisupport.routing.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.aisupport.routing.event.TicketAnalyzedEvent;
import com.aisupport.routing.service.RoutingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketAnalyzedConsumer {
	private final RoutingService routingService;

    @KafkaListener(topics = "ticket-analyzed", groupId = "routing-group")
    public void consume(
            @Payload(required = false) TicketAnalyzedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        // ErrorHandlingDeserializer sets payload to null on bad messages
        // check for it explicitly to avoid NullPointerException
        if (event == null) {
            log.error("Deserialization failed for message on topic={} offset={} — skipping",
                    topic, offset);
            return; // skip gracefully, no crash, no retry loop
        }

        try {
            log.info("Received analyzed ticket: ticketId={} intent={} urgency={}",
                    event.getTicketId(), event.getIntent(), event.getUrgency());

            routingService.route(event);

        } catch (Exception e) {
            log.error("Error routing ticket ticketId={}: {}",
                    event.getTicketId(), e.getMessage(), e);
        }
    }
}
