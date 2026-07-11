package com.aisupport.orchestration.application.workflow.context;

import com.aisupport.orchestration.domain.workflow.WorkflowContext;

public interface ContextProvider {
    boolean supports(String workflowId);
    void populate(WorkflowContext context);
}
