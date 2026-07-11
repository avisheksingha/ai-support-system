package com.aisupport.orchestration.domain.tool;

import com.aisupport.orchestration.domain.model.ToolResult;

public interface ToolDefinition {
    ToolDescriptor getDescriptor();
    ToolResult execute(Object input);
}
