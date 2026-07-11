package com.aisupport.orchestration.infrastructure.mcp;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Interface representing a client that connects to an external MCP (Model Context Protocol) Server.
 */
public interface McpClient {
    /**
     * Discovers all available tools from the connected MCP Server.
     * @return CompletableFuture of tool metadata list
     */
    CompletableFuture<List<McpToolMetadata>> discoverTools();
    
    /**
     * Executes a specific tool on the MCP Server.
     * @param toolName The name of the tool to execute
     * @param arguments The arguments to pass to the tool
     * @return CompletableFuture of the execution result payload
     */
    CompletableFuture<Map<String, Object>> executeTool(String toolName, Map<String, Object> arguments);
}
