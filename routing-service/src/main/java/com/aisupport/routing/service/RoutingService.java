package com.aisupport.routing.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.common.event.TicketAnalyzedEvent;
import com.aisupport.common.event.TicketRoutedEvent;
import com.aisupport.routing.entity.RoutingRule;
import com.aisupport.routing.outbox.OutboxEventService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutingService {

    private final RuleEvaluationService ruleEvaluationService;
    private final OutboxEventService outboxService;

    @Transactional
    public void route(TicketAnalyzedEvent event) {

        Long ticketId = event.getTicketId();

        log.info("Routing ticketId={} using DB rules", ticketId);

        RoutingRule rule = ruleEvaluationService.evaluate(event);

        String team = rule != null ? rule.getAssignToTeam() : "general-support";
        String priority = rule != null && rule.getPriorityOverride() != null
                ? rule.getPriorityOverride()
                : "MEDIUM";

        Integer sla = rule != null && rule.getSlaHours() != null
                ? rule.getSlaHours()
                : 24;

        TicketRoutedEvent routedEvent = TicketRoutedEvent.builder()
                .ticketId(ticketId)
                .assignToTeam(team)
                .priority(priority)
                .slaHours(sla)
                .intent(event.getIntent())
                .sentiment(event.getSentiment())
                .urgency(event.getUrgency())
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
}