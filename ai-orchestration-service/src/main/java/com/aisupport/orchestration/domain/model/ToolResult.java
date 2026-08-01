package com.aisupport.orchestration.domain.model;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ToolResult {
    private boolean success;
    private Object data;
    private Map<String, Object> metadata;
    private long executionTimeMs;
    private String error;
    
    public static ToolResult success(Object data, long executionTimeMs) {
        return ToolResult.builder()
                .success(true)
                .data(data)
                .executionTimeMs(executionTimeMs)
                .build();
    }
    
    public static ToolResult failure(String error, long executionTimeMs) {
        return ToolResult.builder()
                .success(false)
                .error(error)
                .executionTimeMs(executionTimeMs)
                .build();
    }

    public <T> T getData(Class<T> type) {
        if (data != null && type.isInstance(data)) {
            return type.cast(data);
        }
        return null;
    }
}
