package com.aisupport.auth.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {

	@Bean
	OpenAPI customOpenAPI() {
		
		final String securitySchemeName = "bearerAuth";
		
		return new OpenAPI()
			.info(new Info()
				.title("Auth Service API")
				.version("1.0.0")
				.description("API for managing user authentication and authorization in the AI Support System")
				.contact(new Contact()
					.name("AI Support Team")
					.email("support@aisupport.com"))
				.license(new License()
					.name("Apache 2.0")
					.url("https://www.apache.org/licenses/LICENSE-2.0")))
			.servers(List.of(
				new Server().url("http://localhost:8081").description("Development"),
				new Server().url("http://localhost:8080").description("API Gateway")
			))
			.components(new Components()
				.addSecuritySchemes(securitySchemeName, new SecurityScheme()
					.name(securitySchemeName)
					.type(SecurityScheme.Type.HTTP)
					.scheme("bearer")
					.bearerFormat("JWT")));
	}
}
