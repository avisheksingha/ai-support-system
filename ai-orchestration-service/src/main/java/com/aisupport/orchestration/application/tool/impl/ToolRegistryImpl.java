package com.aisupport.orchestration.application.tool.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.aisupport.orchestration.domain.tool.ToolDefinition;
import com.aisupport.orchestration.domain.tool.ToolProvider;
import com.aisupport.orchestration.domain.tool.ToolRegistry;

@Component
public class ToolRegistryImpl implements ToolRegistry {

    private final Map<String, ToolDefinition<?, ?>> toolMap = new ConcurrentHashMap<>();
    private final List<ToolProvider> providers;

    public ToolRegistryImpl(List<ToolProvider> providers) {
        this.providers = providers != null ? providers : List.of();
        // Eagerly discover and register all tools on startup
        this.providers.forEach(provider -> {
            provider.discoverTools().forEach(this::register);
        });
    }

    @Override
    public void register(ToolDefinition<?, ?> tool) {
        toolMap.put(tool.getDescriptor().getName(), tool);
    }

    @Override
    public ToolDefinition<?, ?> getTool(String name) {
        return toolMap.get(name);
    }

    @Override
    public List<ToolDefinition<?, ?>> getAllTools() {
        return List.copyOf(toolMap.values());
    }
}
