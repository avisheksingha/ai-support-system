package com.aisupport.orchestration.infrastructure.mcp.postgres;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mcp.postgres")
public class PostgresMcpProperties {

    private boolean enabled;
    private String mode; // "mock" or "real"
    private Map<String, PostgresConnectionConfig> connections = new HashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Map<String, PostgresConnectionConfig> getConnections() {
        return connections;
    }

    public void setConnections(Map<String, PostgresConnectionConfig> connections) {
        this.connections = connections;
    }
}
