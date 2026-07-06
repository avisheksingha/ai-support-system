package com.aisupport.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI gatewayOpenApi() {
    	
        return new OpenAPI()
            .info(new Info()
                .title("API Gateway")
                .description("Central API Gateway and Swagger Aggregator for AI Support System")
                .version("v1.0.0"));
    }
}
