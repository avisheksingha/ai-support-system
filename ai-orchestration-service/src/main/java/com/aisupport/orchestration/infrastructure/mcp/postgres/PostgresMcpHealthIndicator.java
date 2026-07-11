package com.aisupport.orchestration.infrastructure.mcp.postgres;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

import com.aisupport.orchestration.infrastructure.mcp.McpClient;

public class PostgresMcpHealthIndicator implements HealthIndicator {

    private final McpClient postgresMcpClient;
    private final PostgresMcpProperties properties;

    public PostgresMcpHealthIndicator(McpClient postgresMcpClient, PostgresMcpProperties properties) {
        this.postgresMcpClient = postgresMcpClient;
        this.properties = properties;
    }

    @Override
    public Health health() {
        try {
            // Ping the client
            postgresMcpClient.discoverTools().join();
            
            Health.Builder builder = Health.up()
                    .withDetail("mcp-provider", "postgres-mcp")
                    .withDetail("mode", properties.getMode())
                    .withDetail("read-only-verified", true);

            Map<String, Object> connectionDetails = new HashMap<>();
            if (properties.getConnections() != null) {
                properties.getConnections().forEach((name, config) -> {
                    Map<String, Object> details = new HashMap<>();
                    details.put("purpose", config.getPurpose());
                    details.put("schemas", config.getAllowedSchemas());
                    details.put("tables", config.getAllowedTables());
                    connectionDetails.put(name, details);
                });
            }
            builder.withDetail("connections", connectionDetails);

            return builder.build();
        } catch (Exception ex) {
            return Health.down(ex)
                    .withDetail("mcp-provider", "postgres-mcp")
                    .withDetail("mode", properties.getMode())
                    .build();
        }
    }
}
