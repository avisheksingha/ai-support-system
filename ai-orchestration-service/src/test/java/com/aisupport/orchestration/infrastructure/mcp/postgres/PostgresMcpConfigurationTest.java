package com.aisupport.orchestration.infrastructure.mcp.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.aisupport.orchestration.domain.tool.ToolProvider;

class PostgresMcpConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PostgresMcpConfiguration.class));

    @Test
    void testConfigurationDisabledByDefault() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(PostgresMcpConfiguration.class);
            assertThat(context).doesNotHaveBean(ToolProvider.class);
        });
    }

    @Test
    void testConfigurationEnabledWithMockModeAndNamedConnections() {
        contextRunner
            .withPropertyValues(
                "mcp.postgres.enabled=true",
                "mcp.postgres.mode=mock",
                "mcp.postgres.connections.ticket-db.url=jdbc:postgresql://localhost:5432/ticket_db",
                "mcp.postgres.connections.ticket-db.username=readonly_user",
                "mcp.postgres.connections.ticket-db.allowed-schemas[0]=public"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(PostgresMcpConfiguration.class);
                assertThat(context).hasSingleBean(ToolProvider.class);
                assertThat(context).getBean("postgresMcpClient").isInstanceOf(MockPostgresMcpClient.class);
                
                PostgresMcpProperties props = context.getBean(PostgresMcpProperties.class);
                assertThat(props.getConnections()).containsKey("ticket-db");
                PostgresConnectionConfig config = props.getConnections().get("ticket-db");
                assertThat(config.getUrl()).isEqualTo("jdbc:postgresql://localhost:5432/ticket_db");
                assertThat(config.getUsername()).isEqualTo("readonly_user");
                assertThat(config.getAllowedSchemas()).containsExactly("public");
            });
    }
}





