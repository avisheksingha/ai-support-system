package com.aisupport.orchestration.infrastructure.client.exception;

public class RoutingUnavailableException extends RuntimeException {
	
    private static final long serialVersionUID = 3128515185855041805L;

	public RoutingUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
