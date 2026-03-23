package com.aisupport.ticket.exception;

public class InvalidTicketInputException extends RuntimeException {

    private static final long serialVersionUID = 5927028148665387389L;

	public InvalidTicketInputException(String message) {
        super(message);
    }
}
