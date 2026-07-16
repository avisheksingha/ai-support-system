package com.aisupport.orchestration.application.tool;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.domain.model.ToolResult;
import com.aisupport.orchestration.domain.tool.ToolDefinition;
import com.aisupport.orchestration.domain.tool.ToolDescriptor;
import com.aisupport.orchestration.infrastructure.client.AnalysisClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AiAnalysisTool implements ToolDefinition {
    
    private final AnalysisClient analysisClient;

    @Override
    public ToolDescriptor getDescriptor() {
        return ToolDescriptor.builder()
                .name("analyzeTicket")
                .description("Analyzes a support ticket for intent, sentiment, and urgency.")
                .parameters(Map.of("ticketId", Long.class, "content", String.class))
                .returnType(Object.class)
                .version("1.0.0")
                .build();
    }

    @Override
    public ToolResult execute(Object rawInput) {
        if (!(rawInput instanceof Map<?, ?>)) {
            return ToolResult.failure("Input must be a map containing ticketId and content", 0);
        }
        
        Map<?, ?> input = (Map<?, ?>) rawInput;
        Long ticketId = null;
        if (input.get("ticketId") instanceof Number num) {
        	ticketId = num.longValue();
        }
        String content = (String) input.get("content");
        
        if (ticketId == null || content == null) {
            return ToolResult.failure("Missing required parameters: ticketId, content", 0);
        }
        
        long start = System.currentTimeMillis();
        try {
            Result<Object> clientResult = analysisClient.analyze(ticketId, content);
            long executionTime = System.currentTimeMillis() - start;
            
            if (clientResult.isSuccess()) {
                return ToolResult.success(clientResult.getData(), executionTime);
            } else {
                return ToolResult.failure(clientResult.getErrorMessage(), executionTime);
            }
        } catch (Exception e) {
            return ToolResult.failure(e.getMessage(), System.currentTimeMillis() - start);
        }
    }
}
