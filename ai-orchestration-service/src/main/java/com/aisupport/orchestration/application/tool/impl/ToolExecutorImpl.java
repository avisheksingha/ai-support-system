package com.aisupport.orchestration.application.tool.impl;

import org.springframework.stereotype.Component;

import com.aisupport.orchestration.application.tool.ToolExecutor;
import com.aisupport.orchestration.domain.model.ToolResult;
import com.aisupport.orchestration.domain.tool.ToolDefinition;
import com.aisupport.orchestration.domain.tool.ToolRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ToolExecutorImpl implements ToolExecutor {

    private final ToolRegistry toolRegistry;

    @Override
    public ToolResult execute(String toolName, Object input) {
        ToolDefinition tool = toolRegistry.getTool(toolName);
        if (tool == null) {
            log.warn("Capability not found: {}", toolName);
            return ToolResult.failure("Tool capability not found: " + toolName, 0);
        }
        log.info("Executing capability: {}", toolName);
        return tool.execute(input);
    }
}
