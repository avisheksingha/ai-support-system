package com.aisupport.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AuthException extends RuntimeException {
	
    private static final long serialVersionUID = -6553267652076595966L;

	public AuthException(String message) {
        super(message);
    }
}
