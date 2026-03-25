package com.aisupport.analysis.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {
	
	private static final String PLACEHOLDER = "##default";
	
	// OpenAPI bean for API documentation
	@Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Analysis Service API")
                        .version("1.0.0")
                        .description("AI-powered ticket analysis using Google Gemini")
                        .contact(new Contact()
                                .name("AI Support Team")
                                .email("support@aisupport.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:8083").description("Development"),
                        new Server().url("http://localhost:8080").description("API Gateway")
                ));
    }

	@Bean
    OpenApiCustomizer removeSwaggerPlaceholderDefaults() {
        return openApi -> {
            Set<Schema<?>> visited = new HashSet<>();
            cleanupComponentSchemas(openApi, visited);
            cleanupPathOperations(openApi, visited);
        };
    }

    private static void cleanupComponentSchemas(OpenAPI openApi, Set<Schema<?>> visited) {
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) return;
        openApi.getComponents().getSchemas().values()
            .forEach(schema -> cleanupSchema(schema, visited));
    }

    private static void cleanupPathOperations(OpenAPI openApi, Set<Schema<?>> visited) {
        if (openApi.getPaths() == null) return;
        openApi.getPaths().values().forEach(pathItem ->
            pathItem.readOperations().forEach(op -> cleanupOperation(op, visited))
        );
    }

    private static void cleanupOperation(Operation operation, Set<Schema<?>> visited) {
        cleanupParameters(operation.getParameters(), visited);

        RequestBody requestBody = operation.getRequestBody();
        if (requestBody != null) cleanupContent(requestBody.getContent(), visited);

        if (operation.getResponses() != null) {
            operation.getResponses().values()
                .forEach(response -> cleanupContent(response.getContent(), visited));
        }
    }

    private static void cleanupParameters(List<Parameter> parameters, Set<Schema<?>> visited) {
        if (parameters == null) return;
        for (Parameter parameter : parameters) {
            if (PLACEHOLDER.equals(parameter.getExample())) parameter.setExample(null);
            cleanupSchema(parameter.getSchema(), visited);
        }
    }

    private static void cleanupContent(Content content, Set<Schema<?>> visited) {
        if (content == null) return;
        for (MediaType mediaType : content.values()) {
            cleanupSchema(mediaType.getSchema(), visited);
        }
    }
    
    private static void cleanupSchema(Schema<?> schema, Set<Schema<?>> visited) {
        if (schema == null || !visited.add(schema)) return;

        if (PLACEHOLDER.equals(schema.getDefault())) schema.setDefault(null);
        if (PLACEHOLDER.equals(schema.getExample())) schema.setExample(null);

        if (schema.getProperties() != null) {
            schema.getProperties().values().forEach(prop -> {
                if (prop instanceof Schema<?> s) cleanupSchema(s, visited);
            });
        }

        cleanupSchema(schema.getItems(), visited);

        if (schema.getAdditionalProperties() instanceof Schema<?> additional) {
            cleanupSchema(additional, visited);
        }

        cleanupSchemaList(schema.getAllOf(), visited);
        cleanupSchemaList(schema.getAllOf(), visited);
        cleanupSchemaList(schema.getAllOf(), visited);
    }

    private static void cleanupSchemaList(List<?> schemas, Set<Schema<?>> visited) {
        if (schemas == null) return;
        schemas.stream()
            .filter(s -> s instanceof Schema<?>)
            .map(s -> (Schema<?>) s)
            .forEach(s -> cleanupSchema(s, visited));
    }
}

