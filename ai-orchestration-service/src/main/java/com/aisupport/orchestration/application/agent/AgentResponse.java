package com.aisupport.orchestration.application.agent;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentResponse {
    private final ResponseType responseType;
    private final String content;
    private final List<ToolCallRequest> toolCalls;
    private final UsageData usage;
    private final FinishReason finishReason;
    private final Map<String, Object> metadata;

    @Data
    @Builder
    public static class ToolCallRequest {
        private String id;
        private String toolName;
        private Map<String, Object> arguments;
    }

    @Data
    @Builder
    public static class UsageData {
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;
    }

    public enum FinishReason {
        STOP,
        TOOL_CALLS,
        LENGTH,
        ERROR
    }
    
    public enum ResponseType {
        FINAL,
        TOOL_REQUEST,
        NEEDS_APPROVAL,
        PARTIAL,
        ERROR
    }

    public boolean isToolCall() {
        return finishReason == FinishReason.TOOL_CALLS || 
               responseType == ResponseType.TOOL_REQUEST || 
               (toolCalls != null && !toolCalls.isEmpty());
    }
}
