package com.aisupport.orchestration.domain.tool;

import java.util.List;

public interface ToolRegistry {
    void register(ToolDefinition tool);
    ToolDefinition getTool(String name);
    List<ToolDefinition> getAllTools();
}
