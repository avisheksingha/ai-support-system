package com.aisupport.orchestration.infrastructure;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer postgres = new PostgreSQLContainer(
    		DockerImageName
	    		.parse("ankane/pgvector:latest")
	    		.asCompatibleSubstituteFor("postgres")
    );
    
    static {
        postgres.withDatabaseName("orchestration_db");
        postgres.withUsername("test");
        postgres.withPassword("test");
        postgres.start();
    }

    static final ConfluentKafkaContainer kafka = new ConfluentKafkaContainer(
    		DockerImageName
    			.parse("confluentinc/cp-kafka:7.5.0")
    );

    static {
        kafka.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Postgres orchestration-db connection
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");

        // Kafka connection
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        
        // Disable real providers by forcing mock
        registry.add("mcp.github.mode", () -> "mock");
        registry.add("mcp.filesystem.mode", () -> "mock");
        registry.add("mcp.postgres.mode", () -> "mock");
    }
}




