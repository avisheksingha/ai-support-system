package com.aisupport.gateway.filter;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
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

    /**
     * List of public authentication endpoints that do not require JWT validation.
     * Requests to these endpoints will bypass the JWT authentication filter.
     * 
     */
    private static final List<String> PUBLIC_AUTH_ENDPOINTS = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "eureka" // Remove this after Eureka is secured
    );

    /**
     * List of infrastructure endpoints that do not require JWT validation.
     */
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/actuator/health",
            "/actuator/info"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    // Constructor for JwtAuthenticationFilter that initializes the JwtUtil and refresh threshold.
    public JwtAuthenticationFilter(
            @Value("${app.jwt.secret}") String secret,
            @Value("${jwt.refresh-threshold-seconds:120}") long refreshThresholdSeconds) {
        this.jwtUtil = new JwtUtil(secret);
        this.refreshThresholdSeconds = refreshThresholdSeconds;
    }

    /**
	 * Filters incoming requests to validate JWT tokens and set user information in headers.
	 *
	 * @param exchange the current server exchange
	 * @param chain    the gateway filter chain
	 * @return a Mono that completes when the filter processing is done
	 */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        boolean isAuthEndpoint = PUBLIC_AUTH_ENDPOINTS.stream()
                .anyMatch(path::equals);
                
        boolean isPublicEndpoint = PUBLIC_ENDPOINTS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        if (isAuthEndpoint || isPublicEndpoint) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders()
                .getFirst(SecurityConstants.AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(SecurityConstants.BEARER_PREFIX.length());

        if (!jwtUtil.isTokenValid(token)) {
            return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
        }

        Instant expiration = jwtUtil.extractExpiration(token);
        long remainingSeconds = Duration.between(Instant.now(), expiration).getSeconds();

        if (remainingSeconds <= refreshThresholdSeconds) {
            exchange.getResponse().getHeaders().add(
                    SecurityConstants.HEADER_ACCESS_TOKEN_REFRESH,
                    "true");
        }

        exchange = exchange.mutate()
                .request(builder -> builder
                        .header(SecurityConstants.HEADER_USER_ID, jwtUtil.extractUserId(token))
                        .header(SecurityConstants.HEADER_USER_ROLE, jwtUtil.extractRole(token))
                        .header(SecurityConstants.HEADER_USER_EMAIL, jwtUtil.extractEmail(token)))
                .build();

        return chain.filter(exchange);
    }

    /**
	 * Handles authentication errors by logging the error and setting the appropriate HTTP status code in the response.
	 *
	 * @param exchange the current server exchange
	 * @param message  the error message to log
	 * @param status   the HTTP status to set in the response
	 * @return a Mono that completes when the response is set
	 */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {

		log.warn("Authentication failed: {} ({})", message, status.value());
		
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(status);
		
		return response.setComplete();
	}

    /**
	 * Specifies the order of this filter. A lower value means higher priority.
	 *
	 * @return the order of this filter
	 */
    @Override
    public int getOrder() {
        return -1; // Execute before routing
    }
}