package com.aisupport.common.exception;

public class ServiceCommunicationException extends RuntimeException {
	
	private static final long serialVersionUID = -5682817045822676469L;

	public ServiceCommunicationException(String message, Throwable cause) {
		super(message, cause);
	}

}
