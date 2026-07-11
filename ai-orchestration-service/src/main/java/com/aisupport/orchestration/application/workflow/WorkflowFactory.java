package com.aisupport.orchestration.application.workflow;

import com.aisupport.orchestration.domain.workflow.WorkflowDefinition;

public interface WorkflowFactory {
    WorkflowDefinition create(String workflowId);
}
