package com.aisupport.orchestration.application.workflow.steps;

import org.springframework.stereotype.Component;

import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.domain.workflow.WorkflowStep;

@Component
public class KnowledgeSearchStep implements WorkflowStep {
    @Override
    public String getName() {
        return "Knowledge Search";
    }

    @Override
    public void execute(WorkflowContext context) {
        context.putAttribute("knowledgeSearched", true);
    }
}
