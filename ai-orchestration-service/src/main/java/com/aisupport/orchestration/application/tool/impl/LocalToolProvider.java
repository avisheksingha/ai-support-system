package com.aisupport.orchestration.application.tool.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.aisupport.orchestration.domain.tool.ToolDefinition;
import com.aisupport.orchestration.domain.tool.ToolProvider;

@Component
public class LocalToolProvider implements ToolProvider {
    
    private final List<ToolDefinition> localTools;
    
    public LocalToolProvider(List<ToolDefinition> localTools) {
        this.localTools = localTools != null ? localTools : List.of();
    }
    
    @Override
    public List<ToolDefinition> discoverTools() {
        return localTools;
    }
}
