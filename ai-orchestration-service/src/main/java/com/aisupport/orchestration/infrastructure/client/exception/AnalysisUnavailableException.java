package com.aisupport.orchestration.infrastructure.client.exception;

public class AnalysisUnavailableException extends RuntimeException {
	
    private static final long serialVersionUID = 8687008884126428184L;

	public AnalysisUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
