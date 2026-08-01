package com.aisupport.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutingConfig {

    private static final String REWRITE_REPLACEMENT = "/${segment}";

    @Value("${api.services.auth.url:lb://AUTH-SERVICE}")
    private String authServiceUrl;

    @Value("${api.services.ticket.url:lb://TICKET-SERVICE}")
    private String ticketServiceUrl;

    @Value("${api.services.ai-analysis.url:lb://AI-ANALYSIS-SERVICE}")
    private String analysisServiceUrl;

    @Value("${api.services.routing.url:lb://ROUTING-SERVICE}")
    private String routingServiceUrl;

    @Value("${api.services.rag.url:lb://RAG-SERVICE}")
    private String ragServiceUrl;

    @Value("${api.services.ai-orchestration.url:lb://AI-ORCHESTRATION-SERVICE}")
    private String orchestrationServiceUrl;

    @Bean
    RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    	
        return builder.routes()
            // API Routes
            .route("auth-service-route", r -> r.path("/api/v1/auth/**")
        		.uri(authServiceUrl))
            .route("ticket-service-route", r -> r.path("/api/v1/tickets/**")
                .uri(ticketServiceUrl))
            .route("ai-analysis-service-route", r -> r.path("/api/v1/analysis/**")
                .uri(analysisServiceUrl))
            .route("routing-service-route", r -> r.path("/api/v1/routing/**")
                .uri(routingServiceUrl))
            .route("rag-service-route", r -> r.path("/api/v1/rag/**")
                .uri(ragServiceUrl))
            .route("ai-orchestration-service-route", r -> r.path("/api/v1/orchestration/**")
                .uri(orchestrationServiceUrl))

            // WebSocket route (STOMP over SockJS)
            .route("ticket-service-ws", r -> r.path("/ws/**")
                .filters(f -> f.dedupeResponseHeader(
                    "Access-Control-Allow-Origin Access-Control-Allow-Credentials Access-Control-Allow-Methods Access-Control-Allow-Headers",
                    "RETAIN_FIRST"))
                .uri(ticketServiceUrl))
            
            // Swagger Proxy Routes (No escaping needed here!)
            .route("auth-docs", r -> r.path("/auth-docs/**")
                .filters(f -> f.rewritePath("/auth-docs/(?<segment>.*)", REWRITE_REPLACEMENT))
                .uri(authServiceUrl))
            .route("ticket-docs", r -> r.path("/ticket-docs/**")
                .filters(f -> f.rewritePath("/ticket-docs/(?<segment>.*)", REWRITE_REPLACEMENT))
                .uri(ticketServiceUrl))
            .route("analysis-docs", r -> r.path("/analysis-docs/**")
                .filters(f -> f.rewritePath("/analysis-docs/(?<segment>.*)", REWRITE_REPLACEMENT))
                .uri(analysisServiceUrl))
            .route("routing-docs", r -> r.path("/routing-docs/**")
                .filters(f -> f.rewritePath("/routing-docs/(?<segment>.*)", REWRITE_REPLACEMENT))
                .uri(routingServiceUrl))
            .route("rag-docs", r -> r.path("/rag-docs/**")
                .filters(f -> f.rewritePath("/rag-docs/(?<segment>.*)", REWRITE_REPLACEMENT))
                .uri(ragServiceUrl))
            .route("orchestration-docs", r -> r.path("/orchestration-docs/**")
                .filters(f -> f.rewritePath("/orchestration-docs/(?<segment>.*)", REWRITE_REPLACEMENT))
                .uri(orchestrationServiceUrl))
            .build();
    }
}
