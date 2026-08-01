package com.aisupport.orchestration.domain.tool;

import java.util.List;

public interface ToolProvider {
    /**
     * Discovers and returns all tools available from this provider.
     * For local providers, this simply returns pre-registered beans.
     * For external providers (like MCP), this dynamically fetches available tools.
     */
    List<ToolDefinition> discoverTools();
}
