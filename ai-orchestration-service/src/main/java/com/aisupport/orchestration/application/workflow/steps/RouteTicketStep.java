package com.aisupport.orchestration.application.workflow.steps;

import org.springframework.stereotype.Component;

import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.domain.workflow.WorkflowStep;

@Component
public class RouteTicketStep implements WorkflowStep {
    @Override
    public String getName() {
        return "Route Ticket";
    }

    @Override
    public void execute(WorkflowContext context) {
        context.putAttribute("routed", true);
    }
}
