package com.aisupport.routing.exception;

public class RoutingException extends RuntimeException {

	private static final long serialVersionUID = -5551891629775457456L;

	public RoutingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public RoutingException(String message) {
        super(message);
    }
}