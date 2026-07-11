package com.aisupport.orchestration.domain.tool;

import com.aisupport.orchestration.domain.model.ToolResult;

public interface ToolDefinition<I, O> {
    ToolDescriptor getDescriptor();
    ToolResult<O> execute(Object input);
}
