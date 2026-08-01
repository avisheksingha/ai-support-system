package com.aisupport.orchestration.infrastructure.mcp.filesystem;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

import com.aisupport.orchestration.infrastructure.mcp.McpClient;

public class FilesystemMcpHealthIndicator implements HealthIndicator {

    private final McpClient filesystemMcpClient;
    private final FilesystemMcpProperties properties;

    public FilesystemMcpHealthIndicator(McpClient filesystemMcpClient, FilesystemMcpProperties properties) {
        this.filesystemMcpClient = filesystemMcpClient;
        this.properties = properties;
    }

    @Override
    public Health health() {
        try {
            // Ping the client
            filesystemMcpClient.discoverTools().join();
            return Health.up()
                    .withDetail("mcp-provider", "filesystem-mcp")
                    .withDetail("workspace-accessible", true)
                    .withDetail("allowed-roots-count", properties.getAllowedPaths() != null ? properties.getAllowedPaths().size() : 0)
                    .withDetail("mode", properties.getMode())
                    .build();
        } catch (Exception ex) {
            return Health.down(ex)
                    .withDetail("mcp-provider", "filesystem-mcp")
                    .withDetail("workspace-accessible", false)
                    .withDetail("mode", properties.getMode())
                    .build();
        }
    }
}
