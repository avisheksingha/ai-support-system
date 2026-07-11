package com.aisupport.orchestration.domain.model;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ToolResult<T> {
    private boolean success;
    private T data;
    private Map<String, Object> metadata;
    private long executionTimeMs;
    private String error;
    
    public static <T> ToolResult<T> success(T data, long executionTimeMs) {
        return ToolResult.<T>builder()
                .success(true)
                .data(data)
                .executionTimeMs(executionTimeMs)
                .build();
    }
    
    public static <T> ToolResult<T> failure(String error, long executionTimeMs) {
        return ToolResult.<T>builder()
                .success(false)
                .error(error)
                .executionTimeMs(executionTimeMs)
                .build();
    }
}
