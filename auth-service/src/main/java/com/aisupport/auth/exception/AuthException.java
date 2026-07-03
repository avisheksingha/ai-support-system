package com.aisupport.auth.exception;

import org.springframework.http.HttpStatus;

public class AuthException extends RuntimeException {
	
    private static final long serialVersionUID = -6553267652076595966L;

    private final HttpStatus status;

	public AuthException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
