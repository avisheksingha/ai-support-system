package com.aisupport.orchestration.domain.state;

public enum WorkflowState {
    CREATED,
    STARTED,
    RUNNING,
    WAITING_TOOL,
    WAITING_APPROVAL,
    RETRYING,
    FAILED,
    COMPLETED,
    ROLLED_BACK,
    SKIPPED
}
