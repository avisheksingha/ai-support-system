package com.aisupport.routing.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.aisupport.common.exception.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RoutingException.class)
    public ResponseEntity<ErrorResponse> handleRoutingException(
            RoutingException ex, WebRequest request) {
        log.error("Routing error: {}", ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Routing Error",
                ex.getMessage(),
                request.getDescription(false)
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    @ExceptionHandler(ServiceCommunicationException.class)
    public ResponseEntity<ErrorResponse> handleServiceCommunicationException(
            ServiceCommunicationException ex, WebRequest request) {
        log.error("Service communication error: {}", ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Communication Error",
                ex.getMessage(),
                request.getDescription(false)
        );
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }
    
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleWebClientException(
            WebClientResponseException ex, WebRequest request) {
        log.error("WebClient error: {} - {}", ex.getStatusCode(), ex.getMessage());
        
        String errorMessage = "Downstream service error";
        if (ex.getStatusCode().is4xxClientError()) {
            errorMessage = "Invalid request to downstream service";
        } else if (ex.getStatusCode().is5xxServerError()) {
            errorMessage = "Downstream service unavailable";
        }
        
        ErrorResponse error = ErrorResponse.of(
                ex.getStatusCode().value(),
                "WebClient Error",
                errorMessage,
                request.getDescription(false)
        );
        
        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Invalid request data",
                request.getDescription(false),
                errors
        );
        
        return ResponseEntity.badRequest().body(error);
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