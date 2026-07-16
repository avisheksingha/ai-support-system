package com.aisupport.orchestration.application.workflow;

import java.util.List;

import org.springframework.stereotype.Component;

import com.aisupport.orchestration.application.workflow.steps.AnalyzeTicketStep;
import com.aisupport.orchestration.application.workflow.steps.AssembleContextStep;
import com.aisupport.orchestration.application.workflow.steps.KnowledgeSearchStep;
import com.aisupport.orchestration.application.workflow.steps.RouteTicketStep;
import com.aisupport.orchestration.domain.workflow.WorkflowDefinition;
import com.aisupport.orchestration.domain.workflow.WorkflowStep;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AnalyzeWorkflowDefinition implements WorkflowDefinition {
    
    private final AssembleContextStep assembleContextStep;
    private final AnalyzeTicketStep analyzeTicketStep;
    private final KnowledgeSearchStep knowledgeSearchStep;
    private final RouteTicketStep routeTicketStep;
    
    @Override
    public String getId() {
        return "analyze-workflow";
    }

    @Override
    public String getName() {
        return "Analyze Workflow";
    }

    @Override
    public String getDescription() {
        return "Analyzes a newly created ticket, searches knowledge, and routes it.";
    }

    @Override
    public String getSupportedTrigger() {
        return "ticket-created";
    }

    @Override
    public List<WorkflowStep> getSteps() {
        return List.of(
            assembleContextStep,
            analyzeTicketStep,
            knowledgeSearchStep,
            routeTicketStep
        );
    }
}
