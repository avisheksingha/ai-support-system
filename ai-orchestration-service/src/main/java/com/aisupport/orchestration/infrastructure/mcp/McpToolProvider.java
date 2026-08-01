package com.aisupport.orchestration.infrastructure.mcp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.aisupport.orchestration.domain.model.ToolResult;
import com.aisupport.orchestration.domain.tool.ToolDefinition;
import com.aisupport.orchestration.domain.tool.ToolDescriptor;
import com.aisupport.orchestration.domain.tool.ToolProvider;

/**
 * Adapter that dynamically discovers capabilities from an external MCP server
 * and translates them into internal ToolDefinitions for the Workflow Engine.
 */
public class McpToolProvider implements ToolProvider {
    private final McpClient mcpClient;

    public McpToolProvider(McpClient mcpClient) {
        this.mcpClient = mcpClient;
    }

    @Override
    public List<ToolDefinition> discoverTools() {
        // Step 1: Discover metadata from external MCP Server
        List<McpToolMetadata> mcpTools = mcpClient.discoverTools().join();
        
        // Step 2: Map each MCP tool to our internal ToolDefinition
        return mcpTools.stream().map(metadata -> new ToolDefinition() {
            @Override
            public ToolDescriptor getDescriptor() {
                return ToolDescriptor.builder()
                        .name(metadata.getName())
                        .description(metadata.getDescription())
                        // Note: A real implementation would parse the JSON schema into specific Java Classes
                        .parameters(Map.of("payload", Map.class)) 
                        .returnType(Map.class)
                        .version("1.0.0-MCP")
                        .build();
            }

            @Override
            public ToolResult execute(Object rawInput) {
                if (!(rawInput instanceof Map<?, ?>)) {
                    return ToolResult.failure("Input must be a Map", 0);
                }
                
                // Safely construct the typed Map without unchecked cast warnings
                Map<?, ?> rawMap = (Map<?, ?>) rawInput;
                Map<String, Object> input = new HashMap<>();
                
                for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                    Object key = entry.getKey();
                    if (!(key instanceof String)) {
                        return ToolResult.failure("Input map keys must be of type String", 0);
                    }
                    // (String) key is a checked cast because we validated it with instanceof.
                    // entry.getValue() is already typed as Object, so it fits Map<String, Object>.
                    input.put((String) key, entry.getValue());
                }
                
                long startTime = System.currentTimeMillis();
                try {
                    Map<String, Object> result = mcpClient.executeTool(metadata.getName(), input).join();
                    long duration = System.currentTimeMillis() - startTime;
                    return ToolResult.success(result, duration);
                } catch (Exception ex) {
                    long duration = System.currentTimeMillis() - startTime;
                    return ToolResult.failure("MCP Execution Failed: " + ex.getMessage(), duration);
                }
            }
        }).collect(Collectors.toList());
    }
}

