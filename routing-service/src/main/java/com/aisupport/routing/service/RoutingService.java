package com.aisupport.routing.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.common.enums.TicketPriority;
import com.aisupport.common.event.EventType;
import com.aisupport.common.event.TicketAnalyzedEvent;
import com.aisupport.common.event.TicketRoutedEvent;
import com.aisupport.routing.dto.response.RoutingResponse;
import com.aisupport.routing.entity.RoutingRule;
import com.aisupport.routing.outbox.OutboxEventService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
/**
 * Evaluates routing rules for analyzed tickets and publishes routing outcomes.
 */
public class RoutingService {

    private final RuleEvaluationService ruleEvaluationService;
    private final OutboxEventService outboxService;

    /**
     * Resolves assignment, priority, and SLA for a ticket analysis result,
     * then publishes a {@code TicketRoutedEvent} via outbox.
     *
     * @param event analyzed ticket event
     */
    @Transactional
    public void route(TicketAnalyzedEvent event) {

        Long ticketId = event.getTicketId();

        log.info("Routing ticketId={} using DB rules", ticketId);

        RoutingRule rule = ruleEvaluationService.evaluate(event);

        String team = rule != null ? rule.getAssignToTeam() : "general-support";
        
        TicketPriority priority = rule != null && rule.getPriorityOverride() != null
                ? rule.getPriorityOverride()
                : TicketPriority.MEDIUM;

        Integer sla = rule != null && rule.getSlaHours() != null
                ? rule.getSlaHours()
                : 24;

        TicketRoutedEvent routedEvent = TicketRoutedEvent.builder()
                .ticketId(ticketId)
                .assignToTeam(team)
                .priority(priority)
                .slaHours(sla)
                .intent(event.getAnalysis().intent())
                .sentiment(event.getAnalysis().sentiment())
                .urgency(event.getAnalysis().urgency())
                .build();

        outboxService.publishEvent(
                "TICKET",
                ticketId.toString(),
                EventType.TICKET_ROUTED,
                routedEvent
        );

        log.info("Routing completed ticketId={} team={} priority={}", ticketId, team, priority);
    }

    @Transactional
    public TicketRoutedEvent routeSync(TicketAnalyzedEvent event) {
        Long ticketId = event.getTicketId();
        log.info("Running sync routing for ticketId={}", ticketId);

        RoutingRule rule = ruleEvaluationService.evaluate(event);

        String team = rule != null ? rule.getAssignToTeam() : "general-support";
        TicketPriority priority = rule != null && rule.getPriorityOverride() != null
                ? rule.getPriorityOverride()
                : TicketPriority.MEDIUM;

        Integer sla = rule != null && rule.getSlaHours() != null
                ? rule.getSlaHours()
                : 24;

        TicketRoutedEvent routedEvent = TicketRoutedEvent.builder()
                .ticketId(ticketId)
                .assignToTeam(team)
                .priority(priority)
                .slaHours(sla)
                .intent(event.getAnalysis().intent())
                .sentiment(event.getAnalysis().sentiment())
                .urgency(event.getAnalysis().urgency())
                .build();

        log.info("Sync Routing completed ticketId={} team={} priority={}", ticketId, team, priority);
        return routedEvent;
    }
    
    /**
     * Retrieves the routing execution result for a specific ticket.
     * 
     * @param ticketId the ticket ID
     * @return RoutingResponse containing the reasoning and rule matched
     */
    @Transactional(readOnly = true)
    public RoutingResponse getRoutingForTicket(Long ticketId) {
        log.info("Fetching routing result for ticketId={}", ticketId);
        
        // Use history to find the matched rule
        return ruleEvaluationService.getMatchedExecution(ticketId)
            .map(history -> {
                RoutingRule rule = ruleEvaluationService.getRuleById(history.getRuleId());
                
                return RoutingResponse.builder()
                    .id(history.getId())
                    .ticketId(ticketId)
                    .department(rule != null ? rule.getAssignToTeam() : "General Support")
                    .confidenceScore(0.95) // High confidence for deterministic rules
                    .reason(rule != null ? rule.getDescription() : "Fallback routing")
                    .ruleName(rule != null ? rule.getRuleName() : "FALLBACK_RULE")
                    .ruleVersion(rule != null ? rule.getRuleVersion() : 1)
                    .executedAt(history.getExecutedAt())
                    .build();
            })
            .orElseGet(() -> RoutingResponse.builder()
                .ticketId(ticketId)
                .department("General Support")
                .confidenceScore(0.5)
                .reason("No routing rule matched.")
                .build());
    }
}
