package com.aisupport.ticket.exception;

import com.aisupport.common.exception.ResourceNotFoundException;

public class TicketNotFoundException extends ResourceNotFoundException {
	
	private static final long serialVersionUID = 6296504354558313719L;

	public TicketNotFoundException(String message) {
		super(message);
	}
}
