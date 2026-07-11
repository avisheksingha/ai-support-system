package com.aisupport.orchestration.application.tool;

import com.aisupport.orchestration.domain.model.ToolResult;

public interface ToolExecutor {
    ToolResult<?> execute(String toolName, Object input);
}
