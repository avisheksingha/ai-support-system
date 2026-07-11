package com.aisupport.orchestration.infrastructure.mcp.github;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

import com.aisupport.orchestration.infrastructure.mcp.McpClient;

public class GithubMcpHealthIndicator implements HealthIndicator {

    private final McpClient githubMcpClient;

    public GithubMcpHealthIndicator(McpClient githubMcpClient) {
        this.githubMcpClient = githubMcpClient;
    }

    @Override
    public Health health() {
        try {
            // Ping the client
            githubMcpClient.discoverTools().join();
            return Health.up().withDetail("mcp-provider", "github-mcp").build();
        } catch (Exception ex) {
            return Health.down(ex).withDetail("mcp-provider", "github-mcp").build();
        }
    }
}
