package com.aisupport.orchestration.infrastructure.mcp.filesystem;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.aisupport.orchestration.domain.tool.ToolProvider;

class FilesystemMcpConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FilesystemMcpConfiguration.class));

    @Test
    void testConfigurationDisabledByDefault() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(FilesystemMcpConfiguration.class);
            assertThat(context).doesNotHaveBean(ToolProvider.class);
        });
    }

    @Test
    void testConfigurationEnabledWithMockMode() {
        contextRunner
            .withPropertyValues(
                "mcp.filesystem.enabled=true",
                "mcp.filesystem.mode=mock",
                "mcp.filesystem.workspace-root=/tmp/workspace",
                "mcp.filesystem.allowed-paths[0]=docs/"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(FilesystemMcpConfiguration.class);
                assertThat(context).hasSingleBean(ToolProvider.class);
                assertThat(context).getBean("filesystemMcpClient").isInstanceOf(MockFilesystemMcpClient.class);
                
                FilesystemMcpProperties props = context.getBean(FilesystemMcpProperties.class);
                assertThat(props.getWorkspaceRoot()).isEqualTo("/tmp/workspace");
                assertThat(props.getAllowedPaths()).containsExactly("docs/");
            });
    }
}





