package com.aisupport.orchestration.infrastructure.mcp.exception;

public class McpTransientException extends McpException {
    private static final long serialVersionUID = 1L;

    public McpTransientException(String message) {
        super(message);
    }
}
