package com.aisupport.orchestration.application.workflow;

public interface WorkflowTriggerRegistry {
    void registerTrigger(String triggerEventName, String workflowId);
    String getWorkflowIdForTrigger(String triggerEventName);
}
