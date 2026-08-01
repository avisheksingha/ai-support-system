package com.aisupport.orchestration.domain.workflow;

import java.util.List;

public interface WorkflowDefinition {
    String getId();
    String getName();
    String getDescription();
    String getSupportedTrigger();
    default int getVersion() { return 1; }
    List<WorkflowStep> getSteps();
}
