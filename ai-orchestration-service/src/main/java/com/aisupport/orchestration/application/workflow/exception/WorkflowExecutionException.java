package com.aisupport.orchestration.application.workflow.exception;

public class WorkflowExecutionException extends RuntimeException {	

    private static final long serialVersionUID = -8789164228184702164L;
    
	public WorkflowExecutionException(String message) {
        super(message);
    }
    public WorkflowExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
