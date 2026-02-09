package com.aisupport.ticket.exception;

public class TicketNotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = 6296504354558313719L;

	public TicketNotFoundException(String message) {
		super(message);
	}
}
