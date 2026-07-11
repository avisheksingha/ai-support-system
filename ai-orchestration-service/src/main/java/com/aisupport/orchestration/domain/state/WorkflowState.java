package com.aisupport.orchestration.domain.state;

public enum WorkflowState {
    CREATED,
    RUNNING,
    WAITING_TOOL,
    WAITING_APPROVAL,
    RETRYING,
    FAILED,
    COMPLETED
}
