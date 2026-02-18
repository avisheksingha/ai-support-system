package com.aisupport.ticket.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {
	
	// OpenAPI bean for API documentation
	@Bean
	OpenAPI customOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("Ticket Service API")
				.version("1.0.0")
				.description("API for managing customer support tickets in the AI Support System")
				.contact(new Contact()
					.name("AI Support Team")
					.email("support@aisupport.com"))
				.license(new License()
					.name("Apache 2.0")
					.url("https://www.apache.org/licenses/LICENSE-2.0")))
			.servers(List.of(
				new Server().url("http://localhost:8082").description("Development")/*,
            	new Server().url("http://localhost:8080").description("API Gateway")*/
			));
	}
}


