package com.aisupport.orchestration.application.workflow.steps;

import java.util.Collections;

import org.springframework.stereotype.Component;

import com.aisupport.common.event.KnowledgeContext;
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
        KnowledgeContext knowledge = new KnowledgeContext(
            "Knowledge base suggests performing a password reset.",
            Collections.singletonList("KB-101"),
            0.85
        );
        context.putResource(KnowledgeContext.class, knowledge);
    }
}
