package com.aisupport.common.exception;

public class OutboxEventException extends RuntimeException {
	
	private static final long serialVersionUID = 4796350302055844619L;

	public OutboxEventException(String message) {
        super(message);
    }

	public OutboxEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
