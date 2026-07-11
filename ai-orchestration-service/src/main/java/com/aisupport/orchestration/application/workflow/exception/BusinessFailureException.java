package com.aisupport.orchestration.application.workflow.exception;

public class BusinessFailureException extends WorkflowExecutionException {

    private static final long serialVersionUID = 5910691646264148136L;
    
	public BusinessFailureException(String message) {
        super(message);
    }
    public BusinessFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
