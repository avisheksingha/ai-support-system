package com.aisupport.orchestration.application.tool.impl;

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
public class KnowledgeSearchTool implements ToolDefinition<String, List<Object>> {
    
    private final RagClient ragClient;

    @Override
    public ToolDescriptor getDescriptor() {
        return ToolDescriptor.builder()
                .name("knowledge.search")
                .description("Searches the knowledge base for articles relevant to a query.")
                .parameters(Map.of("query", String.class))
                .returnType(List.class)
                .version("1.0")
                .build();
    }

    @Override
    public ToolResult<List<Object>> execute(Object rawInput) {
        if (!(rawInput instanceof String)) {
            return ToolResult.failure("Input must be a string query", 0);
        }
        String input = (String) rawInput;
        
        long start = System.currentTimeMillis();
        try {
            Result<List<Object>> clientResult = ragClient.searchKnowledge(input);
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
