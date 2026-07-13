package com.aisupport.orchestration.application.workflow.listener;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aisupport.orchestration.application.compliance.AiAuditService;
import com.aisupport.orchestration.application.workflow.WorkflowExecutionListener;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.domain.workflow.WorkflowStep;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditWorkflowExecutionListener implements WorkflowExecutionListener {

    private final AiAuditService auditService;
    
    @Value("${info.build.version:1.0.0}")
    private String serviceVersion;

    @Override
    public void beforeWorkflow(WorkflowContext context) {
        // No-op
    }

    @Override
    public void afterWorkflow(WorkflowContext context) {
        long durationMs = Duration.between(context.getStartTime(), Instant.now()).toMillis();
        auditService.recordWorkflowExecution(
            context.getTicketId(),
            context.getWorkflowId(),
            context.getCorrelationId(),
            "SUCCESS",
            durationMs,
            serviceVersion
        );
    }

    @Override
    public void beforeStep(WorkflowContext context, WorkflowStep step) {
        // No-op
    }

    @Override
    public void afterStep(WorkflowContext context, WorkflowStep step) {
        // No-op
    }

    @Override
    public void onFailure(WorkflowContext context, Throwable error) {
        long durationMs = Duration.between(context.getStartTime(), Instant.now()).toMillis();
        auditService.recordWorkflowExecution(
            context.getTicketId(),
            context.getWorkflowId(),
            context.getCorrelationId(),
            "FAILED",
            durationMs,
            serviceVersion
        );
    }
}
