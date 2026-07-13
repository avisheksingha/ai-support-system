package com.aisupport.orchestration.infrastructure.client.exception;

public class RagUnavailableException extends RuntimeException {	
	
    private static final long serialVersionUID = -3135085502395654518L;

	public RagUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
