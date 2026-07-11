package com.aisupport.orchestration.infrastructure.mcp.github;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aisupport.orchestration.domain.tool.ToolProvider;
import com.aisupport.orchestration.infrastructure.mcp.McpClient;
import com.aisupport.orchestration.infrastructure.mcp.McpToolProvider;

@Configuration
@EnableConfigurationProperties(GithubMcpProperties.class)
@ConditionalOnProperty(name = "mcp.github.enabled", havingValue = "true")
public class GithubMcpConfiguration {

    @Bean
    McpClient githubMcpClient(GithubMcpProperties properties) {
        if ("mock".equalsIgnoreCase(properties.getMode())) {
            return new MockGithubMcpClient();
        } else {
            return new RealGithubMcpClient();
        }
    }

    @Bean
    ToolProvider githubMcpToolProvider(McpClient githubMcpClient) {
        return new McpToolProvider(githubMcpClient);
    }

    @Bean
    GithubMcpHealthIndicator githubMcpHealthIndicator(McpClient githubMcpClient) {
        return new GithubMcpHealthIndicator(githubMcpClient);
    }
}

