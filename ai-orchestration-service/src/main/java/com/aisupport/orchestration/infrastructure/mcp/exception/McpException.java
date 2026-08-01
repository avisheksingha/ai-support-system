package com.aisupport.orchestration.infrastructure.mcp.exception;

public class McpException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public McpException(String message) {
        super(message);
    }
    
    public McpException(String message, Throwable cause) {
        super(message, cause);
    }
}
