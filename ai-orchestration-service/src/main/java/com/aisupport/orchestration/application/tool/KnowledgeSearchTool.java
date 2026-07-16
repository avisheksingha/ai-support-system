package com.aisupport.orchestration.application.tool;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.domain.model.ToolResult;
import com.aisupport.orchestration.domain.tool.ToolDefinition;
import com.aisupport.orchestration.domain.tool.ToolDescriptor;
import com.aisupport.orchestration.infrastructure.client.RagClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KnowledgeSearchTool implements ToolDefinition {
    
    private final RagClient ragClient;

    @Override
    public ToolDescriptor getDescriptor() {
        return ToolDescriptor.builder()
                .name("searchKnowledge")
                .description("Searches the internal knowledge base for relevant articles.")
                .parameters(Map.of("ticketId", Long.class, "query", String.class))
                .returnType(List.class)
                .version("1.0.0")
                .build();
    }

    @Override
    public ToolResult execute(Object rawInput) {
        if (!(rawInput instanceof Map<?, ?>)) {
            return ToolResult.failure("Input must be a map containing ticketId and query", 0);
        }
        
        Map<?, ?> input = (Map<?, ?>) rawInput;
        Long ticketId = null;
        if (input.get("ticketId") instanceof Number num) {
        	ticketId = num.longValue();
        }
        String query = (String) input.get("query");
        
        if (ticketId == null || query == null) {
            return ToolResult.failure("Missing required parameters: ticketId, query", 0);
        }
        
        long start = System.currentTimeMillis();
        try {
            Result<List<Object>> clientResult = ragClient.searchKnowledge(ticketId, query);
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
