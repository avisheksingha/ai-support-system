package com.aisupport.orchestration.application.workflow.listener;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.aisupport.orchestration.application.workflow.WorkflowExecutionListener;
import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.domain.workflow.WorkflowStep;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(10)
public class LoggingWorkflowExecutionListener implements WorkflowExecutionListener {
    @Override
    public void beforeWorkflow(WorkflowContext context) {
        log.info("Workflow starting: executionId={}", context.getExecutionId());
    }

    @Override
    public void afterWorkflow(WorkflowContext context) {
        log.info("Workflow completed: executionId={}", context.getExecutionId());
    }

    @Override
    public void beforeStep(WorkflowContext context, WorkflowStep step) {
        log.info("Step starting: executionId={}, step={}", context.getExecutionId(), step.getName());
    }

    @Override
    public void afterStep(WorkflowContext context, WorkflowStep step) {
        log.info("Step completed: executionId={}, step={}", context.getExecutionId(), step.getName());
    }

    @Override
    public void onFailure(WorkflowContext context, Throwable error) {
        log.error("Workflow failed: executionId={}, error={}", context.getExecutionId(), error.getMessage());
    }
}
