package com.aisupport.orchestration.application.workflow;

import com.aisupport.orchestration.domain.workflow.WorkflowContext;

public interface WorkflowEngine {
    WorkflowExecutionResult execute(String workflowId, WorkflowContext context);
    WorkflowExecutionResult resume(String executionId);
}
