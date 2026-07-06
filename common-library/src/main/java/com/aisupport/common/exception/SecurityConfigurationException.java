package com.aisupport.common.exception;

/**
 * Custom runtime exception thrown when the Spring Security filter chain
 * fails to initialize or build properly.
 */
public class SecurityConfigurationException extends RuntimeException {
    
    private static final long serialVersionUID = 3566218810600642854L;

	public SecurityConfigurationException(String message) {
        super(message);
    }

    public SecurityConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
