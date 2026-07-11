package com.aisupport.orchestration.application.workflow;

import com.aisupport.orchestration.domain.workflow.WorkflowContext;
import com.aisupport.orchestration.domain.workflow.WorkflowStep;

public interface WorkflowExecutionListener {
    default void beforeWorkflow(WorkflowContext context) {}
    default void afterWorkflow(WorkflowContext context) {}
    default void beforeStep(WorkflowContext context, WorkflowStep step) {}
    default void afterStep(WorkflowContext context, WorkflowStep step) {}
    default void onFailure(WorkflowContext context, Throwable error) {}
}
