package com.aisupport.orchestration.application.workflow;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.domain.workflow.WorkflowStep;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(10)
public class LoggingWorkflowExecutionListener implements WorkflowExecutionListener {
    @Override
    public void beforeWorkflow(WorkflowContext context) {
        log.info("Workflow Started - executionId={}, workflowId={}", context.getExecutionId(), context.getWorkflowId());
    }

    @Override
    public void afterWorkflow(WorkflowContext context) {
        log.info("Workflow Completed - executionId={}", context.getExecutionId());
    }

    @Override
    public void beforeStep(WorkflowContext context, WorkflowStep step) {
        log.info("Workflow Step Started - step={}", step.getName());
    }

    @Override
    public void afterStep(WorkflowContext context, WorkflowStep step) {
        log.info("Workflow Step Completed - step={}", step.getName());
    }

    @Override
    public void onFailure(WorkflowContext context, Throwable error) {
        log.error("Workflow Failed - executionId={}, error={}", context.getExecutionId(), error.getMessage());
    }
}
