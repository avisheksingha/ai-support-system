package com.aisupport.routing.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.routing.event.TicketAnalyzedEvent;
import com.aisupport.routing.event.TicketRoutedEvent;
import com.aisupport.routing.outbox.OutboxEventService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutingService {

    private final OutboxEventService outboxService;

    @Transactional
    public void route(TicketAnalyzedEvent event) {

    	Long ticketId = event.getTicketId();

        log.info("Routing ticketId={} intent={} urgency={}",
                ticketId, event.getIntent(), event.getUrgency());

        String team = determineTeam(event);
        String priority = determinePriority(event);

        TicketRoutedEvent routedEvent = TicketRoutedEvent.builder()
                .ticketId(ticketId)
                .assignToTeam(team)
                .priority(priority)
                .slaHours(determineSla(event))
                .build();

        outboxService.publishEvent(
                "TICKET",
                ticketId.toString(),
                "TicketRoutedEvent",
                routedEvent
        );

        log.info("Routing completed ticketId={} team={} priority={}",
                ticketId, team, priority);
    }

    // Rule method to determine SLA based on intent and urgency
    private String determineTeam(TicketAnalyzedEvent e) {

        String intent = safe(e.getIntent());

        return switch (intent) {
            case "CHECK_REFUND_STATUS", "PAYMENT_ISSUE" -> "billing-team";
            case "TECHNICAL_ISSUE" -> "tech-support";
            default -> "general-support";
        };
    }

    private String determinePriority(TicketAnalyzedEvent e) {

        String urgency = safe(e.getUrgency());

        return switch (urgency) {
            case "HIGH" -> "HIGH";
            case "MEDIUM" -> "MEDIUM";
            default -> "LOW";
        };
    }
    
    private Integer determineSla(TicketAnalyzedEvent e) {

        return switch (safe(e.getUrgency())) {
            case "HIGH" -> 12;
            case "MEDIUM" -> 24;
            default -> 48;
        };
    }
    
    private String safe(String value) {
		return value == null ? "" : value.trim().toUpperCase();
	}
}