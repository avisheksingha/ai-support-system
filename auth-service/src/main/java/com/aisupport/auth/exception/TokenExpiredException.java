package com.aisupport.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class TokenExpiredException extends RuntimeException {
	
    private static final long serialVersionUID = -5995837700446684043L;

	public TokenExpiredException(String message) {
        super(message);
    }
}
