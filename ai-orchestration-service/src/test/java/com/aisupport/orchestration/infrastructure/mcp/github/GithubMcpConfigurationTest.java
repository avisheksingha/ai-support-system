package com.aisupport.orchestration.infrastructure.mcp.github;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.aisupport.orchestration.domain.tool.ToolProvider;

class GithubMcpConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(GithubMcpConfiguration.class));

    @Test
    void testConfigurationDisabledByDefault() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(GithubMcpConfiguration.class);
            assertThat(context).doesNotHaveBean(ToolProvider.class);
        });
    }

    @Test
    void testConfigurationEnabledWithMockMode() {
        contextRunner
            .withPropertyValues(
                "mcp.github.enabled=true",
                "mcp.github.mode=mock"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(GithubMcpConfiguration.class);
                assertThat(context).hasSingleBean(ToolProvider.class);
                assertThat(context).getBean("githubMcpClient").isInstanceOf(MockGithubMcpClient.class);
            });
    }

    @Test
    void testConfigurationEnabledWithRealMode() {
        contextRunner
            .withPropertyValues(
                "mcp.github.enabled=true",
                "mcp.github.mode=real"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(GithubMcpConfiguration.class);
                assertThat(context).hasSingleBean(ToolProvider.class);
                assertThat(context).getBean("githubMcpClient").isInstanceOf(RealGithubMcpClient.class);
            });
    }
}





