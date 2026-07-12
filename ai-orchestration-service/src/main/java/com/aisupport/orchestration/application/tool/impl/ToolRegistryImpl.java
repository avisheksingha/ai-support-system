package com.aisupport.orchestration.application.tool.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.aisupport.orchestration.domain.tool.ToolDefinition;
import com.aisupport.orchestration.domain.tool.ToolProvider;
import com.aisupport.orchestration.domain.tool.ToolRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ToolRegistryImpl implements ToolRegistry {

    private final Map<String, ToolDefinition> toolMap = new ConcurrentHashMap<>();

    public ToolRegistryImpl(List<ToolProvider> providers) {
        for (ToolProvider provider : providers) {
            try {
                for (ToolDefinition tool : provider.discoverTools()) {
                    register(tool);
                }
            } catch (Exception e) {
                log.warn("Failed to discover tools from provider: {}. Error: {}", provider.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    @Override
    public void register(ToolDefinition tool) {
        toolMap.put(tool.getDescriptor().getName(), tool);
        log.info("Registered tool: {}", tool.getDescriptor().getName());
    }

    @Override
    public ToolDefinition getTool(String name) {
        return toolMap.get(name);
    }

    @Override
    public List<ToolDefinition> getAllTools() {
        return new ArrayList<>(toolMap.values());
    }
}
