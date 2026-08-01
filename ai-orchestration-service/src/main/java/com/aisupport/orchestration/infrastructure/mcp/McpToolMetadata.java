package com.aisupport.orchestration.infrastructure.mcp;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpToolMetadata {
    private String name;
    private String description;
    
    // In MCP, the input schema is typically a JSON Schema object
    private Map<String, Object> inputSchema;
}
