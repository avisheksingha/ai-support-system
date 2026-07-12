package com.aisupport.orchestration.application.workflow.listener;

import org.springframework.stereotype.Component;

import com.aisupport.orchestration.application.compliance.OrchestrationAuditService;
import com.aisupport.orchestration.application.workflow.WorkflowExecutionListener;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.domain.workflow.WorkflowStep;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuditWorkflowExecutionListener implements WorkflowExecutionListener {

    private final OrchestrationAuditService auditService;

    @Override
    public void beforeWorkflow(WorkflowContext context) {
        auditService.logStep(
            context.getTicketId(),
            context.getWorkflowId(),
            context.getCorrelationId(),
            "Workflow Started",
            "INITIALIZE",
            "IN_PROGRESS"
        );
    }

    @Override
    public void afterWorkflow(WorkflowContext context) {
        auditService.logStep(
            context.getTicketId(),
            context.getWorkflowId(),
            context.getCorrelationId(),
            "Workflow Completed",
            "FINALIZE",
            "SUCCESS"
        );
    }

    @Override
    public void beforeStep(WorkflowContext context, WorkflowStep step) {
        auditService.logStep(
            context.getTicketId(),
            context.getWorkflowId(),
            context.getCorrelationId(),
            step.getName(),
            "EXECUTE_STEP_START",
            "PENDING"
        );
    }

    @Override
    public void afterStep(WorkflowContext context, WorkflowStep step) {
        auditService.logStep(
            context.getTicketId(),
            context.getWorkflowId(),
            context.getCorrelationId(),
            step.getName(),
            "EXECUTE_STEP_COMPLETE",
            "SUCCESS"
        );
    }

    @Override
    public void onFailure(WorkflowContext context, Throwable error) {
        auditService.logStep(
            context.getTicketId(),
            context.getWorkflowId(),
            context.getCorrelationId(),
            "Workflow Failed",
            "ERROR",
            error.getMessage() != null ? error.getMessage() : "Unknown Error"
        );
    }
}
