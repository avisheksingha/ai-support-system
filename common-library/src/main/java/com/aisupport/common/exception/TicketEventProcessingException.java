package com.aisupport.common.exception;

public class TicketEventProcessingException extends RuntimeException {	
	
	private static final long serialVersionUID = 6931173577192543960L;

	public TicketEventProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

