package com.aisupport.orchestration.application.workflow.steps;

import org.springframework.stereotype.Component;

import com.aisupport.common.event.AnalysisResult;
import com.aisupport.common.event.RoutingDecision;
import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.domain.workflow.WorkflowStep;
import com.aisupport.orchestration.domain.workflow.WorkflowStepConstants;
import com.aisupport.orchestration.infrastructure.client.RoutingClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouteTicketStep implements WorkflowStep {

    private final RoutingClient routingClient;

    @Override
    public String getName() {
        return WorkflowStepConstants.ROUTE_TICKET;
    }

    @Override
    public void execute(WorkflowContext context) {
        log.info("Routing Ticket via internal domain service for Ticket ID: {}", context.getTicketId());
        
        AnalysisResult analysis = context.getResource(AnalysisResult.class);
        if (analysis == null) {
            log.warn("No AnalysisResult found in context. Routing will be attempted without it.");
        }

        Result<RoutingDecision> result = routingClient.route(context.getTicketId(), analysis);

        if (result.isSuccess()) {
            RoutingDecision decision = result.getData();
            context.putResource(RoutingDecision.class, decision);
            context.putAttribute("routingDecision", decision);
            log.info("Ticket Routed Team={} Priority={}", decision.assignToTeam(), decision.priority());
        } else {
            log.error("Failed to route ticket: {}", result.getErrorMessage());
            // Fallback for resiliency
            RoutingDecision fallback = new RoutingDecision("L1_Support", com.aisupport.common.enums.TicketPriority.MEDIUM, 24);
            context.putResource(RoutingDecision.class, fallback);
            context.putAttribute("routingDecision", fallback);
            log.info("Ticket Routed (Fallback) Team={} Priority={}", fallback.assignToTeam(), fallback.priority());
        }
    }
}
