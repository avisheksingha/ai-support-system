package com.aisupport.routing.exception;

public class ServiceCommunicationException extends RuntimeException {
	
	private static final long serialVersionUID = 1984846275432549820L;

	public ServiceCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}