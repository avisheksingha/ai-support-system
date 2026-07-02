package com.aisupport.gateway.filter;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.aisupport.common.auth.JwtUtil;
import com.aisupport.common.auth.SecurityConstants;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final long refreshThresholdSeconds;

    public JwtAuthenticationFilter(
            @Value("${app.jwt.secret}") String secret,
            @Value("${jwt.refresh-threshold-seconds:120}") long refreshThresholdSeconds) {
        this.jwtUtil = new JwtUtil(secret);
        this.refreshThresholdSeconds = refreshThresholdSeconds;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        final List<String> apiEndpoints = List.of(
                "/api/v1/auth/register",
                "/api/v1/auth/login",
                "/api/v1/auth/refresh",
                "/eureka"
        );

        Predicate<ServerHttpRequest> isApiSecured = r -> apiEndpoints.stream()
                .noneMatch(uri -> r.getURI().getPath().contains(uri));

        if (isApiSecured.test(request)) {
            if (request.getHeaders().getOrEmpty(SecurityConstants.AUTHORIZATION_HEADER).isEmpty()) {
                return this.onError(exchange, "No authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getOrEmpty(SecurityConstants.AUTHORIZATION_HEADER).get(0);
            if (authHeader != null && authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
                String token = authHeader.substring(7);

                if (!jwtUtil.isTokenValid(token)) {
                    return this.onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
                }

                // Check remaining validity and inject refresh header if near expiration
                java.util.Date expiration = jwtUtil.extractExpiration(token);
                java.time.Duration remaining = java.time.Duration.between(java.time.Instant.now(), expiration.toInstant());
                if (remaining.getSeconds() <= refreshThresholdSeconds) {
                    exchange.getResponse().getHeaders().add(SecurityConstants.HEADER_ACCESS_TOKEN_REFRESH, "true");
                }

                // Populate headers for downstream services
                String userId = jwtUtil.extractUserId(token);
                String role = jwtUtil.extractRole(token);
                String email = jwtUtil.extractEmail(token);

                exchange = exchange.mutate()
                        .request(builder -> builder
                                .header(SecurityConstants.HEADER_USER_ID, userId)
                                .header(SecurityConstants.HEADER_USER_ROLE, role)
                                .header(SecurityConstants.HEADER_USER_EMAIL, email)
                        )
                        .build();
            } else {
                return this.onError(exchange, "Invalid authorization header", HttpStatus.UNAUTHORIZED);
            }
        }
        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        log.warn("Authentication failed: {} - Status: {}", err, httpStatus);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1; // Execute before routing
    }
}
