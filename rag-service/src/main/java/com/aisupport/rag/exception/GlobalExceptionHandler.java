package com.aisupport.rag.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.aisupport.common.exception.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	
	@ExceptionHandler(RagGenerationException.class)
	public ResponseEntity<ErrorResponse> handleRagGenerationException(
	        RagGenerationException ex, WebRequest request) {
	    log.error("RAG generation error: {}", ex.getMessage(), ex);

	    ErrorResponse error = ErrorResponse.of(
	            HttpStatus.INTERNAL_SERVER_ERROR.value(),
	            "RAG Generation Error",
	            ex.getMessage(),
	            request.getDescription(false)
	    );

	    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(
            Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                request.getDescription(false)
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}