package com.aisupport.rag.exception;

public class RagGenerationException extends RuntimeException {
	
	private static final long serialVersionUID = 1114665567260377526L;

	public RagGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}