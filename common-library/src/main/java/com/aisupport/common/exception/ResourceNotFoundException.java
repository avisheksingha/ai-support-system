package com.aisupport.common.exception;

public class ResourceNotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = 548970555734155287L;

	public ResourceNotFoundException(String message) {
		super(message);
	}
}
