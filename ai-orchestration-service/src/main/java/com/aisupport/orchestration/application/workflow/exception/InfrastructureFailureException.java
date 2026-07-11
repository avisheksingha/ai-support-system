package com.aisupport.orchestration.application.workflow.exception;

public class InfrastructureFailureException extends WorkflowExecutionException {

    private static final long serialVersionUID = 3539993097604428820L;
    
	public InfrastructureFailureException(String message) {
        super(message);
    }
    public InfrastructureFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
