package com.aisupport.orchestration.application.tool.impl;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.domain.model.ToolResult;
import com.aisupport.orchestration.domain.tool.ToolDefinition;
import com.aisupport.orchestration.domain.tool.ToolDescriptor;
import com.aisupport.orchestration.infrastructure.client.RoutingClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TicketRoutingTool implements ToolDefinition {
    
    private final RoutingClient routingClient;

    @Override
    public ToolDescriptor getDescriptor() {
        return ToolDescriptor.builder()
                .name("routeTicket")
                .description("Assigns a ticket to a team based on analysis results.")
                .parameters(Map.of("ticketId", Long.class, "analysisResult", Object.class))
                .returnType(Object.class)
                .version("1.0.0")
                .build();
    }

    @Override
    public ToolResult execute(Object rawInput) {
        if (!(rawInput instanceof Map<?, ?>)) {
            return ToolResult.failure("Input must be a map containing ticketId and analysisResult", 0);
        }
        
        Map<?, ?> input = (Map<?, ?>) rawInput;
        Long ticketId = null;
        if (input.get("ticketId") instanceof Number num) {
        	ticketId = num.longValue();
        }
        Object analysisResult = input.get("analysisResult");
        
        if (ticketId == null || analysisResult == null) {
            return ToolResult.failure("Missing required parameters: ticketId, analysisResult", 0);
        }
        
        long start = System.currentTimeMillis();
        try {
            Result<Object> clientResult = routingClient.route(ticketId, analysisResult);
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
