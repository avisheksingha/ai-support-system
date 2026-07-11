package com.aisupport.orchestration.application.workflow;

import com.aisupport.orchestration.domain.workflow.WorkflowContext;

public interface ContextBuilder<T> {
    WorkflowContext build(T triggerEvent, String workflowId);
}
