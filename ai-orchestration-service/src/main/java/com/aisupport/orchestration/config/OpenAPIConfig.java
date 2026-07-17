package com.aisupport.orchestration.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {
	
	@Value("${info.app.version:1.0.0}")
	private String serviceVersion;

	@Bean
	OpenAPI customOpenAPI() {
		
		final String securitySchemeName = "bearerAuth";
		
		return new OpenAPI()
			.info(new Info()
				.title("Orchestration Service API")
				.version(serviceVersion)
				.description("API for AI workflow orchestration and operations observability")
				.contact(new Contact()
					.name("AI Support Team")
					.email("support@aisupport.com"))
				.license(new License()
					.name("Apache 2.0")
					.url("https://www.apache.org/licenses/LICENSE-2.0")))
			.servers(List.of(
				new Server().url("http://localhost:8084").description("Development"),
            	new Server().url("http://localhost:8080").description("API Gateway")
			))
			.addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
			.components(new Components()
				.addSecuritySchemes(securitySchemeName, new SecurityScheme()
					.name(securitySchemeName)
					.type(SecurityScheme.Type.HTTP)
					.scheme("bearer")
					.bearerFormat("JWT")));
	}
}
