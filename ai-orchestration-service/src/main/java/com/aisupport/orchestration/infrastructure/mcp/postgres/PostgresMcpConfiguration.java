package com.aisupport.orchestration.infrastructure.mcp.postgres;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aisupport.orchestration.domain.tool.ToolProvider;
import com.aisupport.orchestration.infrastructure.mcp.McpClient;
import com.aisupport.orchestration.infrastructure.mcp.McpToolProvider;

@Configuration
@EnableConfigurationProperties(PostgresMcpProperties.class)
@ConditionalOnProperty(name = "mcp.postgres.enabled", havingValue = "true")
public class PostgresMcpConfiguration {

    @Bean
    McpClient postgresMcpClient(PostgresMcpProperties properties) {
        if ("mock".equalsIgnoreCase(properties.getMode())) {
            return new MockPostgresMcpClient(properties);
        } else {
            return new RealPostgresMcpClient();
        }
    }

    @Bean
    ToolProvider postgresMcpToolProvider(McpClient postgresMcpClient) {
        return new McpToolProvider(postgresMcpClient);
    }

    @Bean
    PostgresMcpHealthIndicator postgresMcpHealthIndicator(McpClient postgresMcpClient, PostgresMcpProperties properties) {
        return new PostgresMcpHealthIndicator(postgresMcpClient, properties);
    }
}

