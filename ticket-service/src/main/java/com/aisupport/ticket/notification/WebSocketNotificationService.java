package com.aisupport.ticket.notification;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.aisupport.common.event.DomainEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastEvent(DomainEvent<?> event) {
        String topic = "/topic/tickets." + event.getEntityId();
        log.info("Broadcasting event {} to topic {}", event.getEventType(), topic);
        messagingTemplate.convertAndSend(topic, event);
    }

    /**
     * Broadcasts a domain event to a ticket-number-based topic.
     * Both agent and customer UIs subscribe using the ticket number,
     * so this method ensures real-time delivery to both parties.
     */
    public void broadcastEvent(DomainEvent<?> event, String ticketNumber) {
        String topic = "/topic/tickets." + ticketNumber;
        log.info("Broadcasting event {} to topic {}", event.getEventType(), topic);
        messagingTemplate.convertAndSend(topic, event);
    }
}
