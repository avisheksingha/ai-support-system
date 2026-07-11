package com.aisupport.orchestration.infrastructure.mcp.filesystem;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aisupport.orchestration.domain.tool.ToolProvider;
import com.aisupport.orchestration.infrastructure.mcp.McpClient;
import com.aisupport.orchestration.infrastructure.mcp.McpToolProvider;

@Configuration
@EnableConfigurationProperties(FilesystemMcpProperties.class)
@ConditionalOnProperty(name = "mcp.filesystem.enabled", havingValue = "true")
public class FilesystemMcpConfiguration {

    @Bean
    McpClient filesystemMcpClient(FilesystemMcpProperties properties) {
        if ("mock".equalsIgnoreCase(properties.getMode())) {
            return new MockFilesystemMcpClient(properties);
        } else {
            return new RealFilesystemMcpClient();
        }
    }

    @Bean
    ToolProvider filesystemMcpToolProvider(McpClient filesystemMcpClient) {
        return new McpToolProvider(filesystemMcpClient);
    }

    @Bean
    FilesystemMcpHealthIndicator filesystemMcpHealthIndicator(McpClient filesystemMcpClient, FilesystemMcpProperties properties) {
        return new FilesystemMcpHealthIndicator(filesystemMcpClient, properties);
    }
}

