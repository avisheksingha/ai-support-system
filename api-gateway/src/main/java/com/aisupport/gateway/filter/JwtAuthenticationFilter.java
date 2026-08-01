package com.aisupport.gateway.filter;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

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

import com.aisupport.common.security.CommonSecurityEndpoints;
import com.aisupport.common.security.JwtUtil;
import com.aisupport.common.security.SecurityConstants;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
	
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final JwtUtil jwtUtil;
    private final long refreshThresholdSeconds;

    /**
     * Public authentication endpoints (register/login/refresh) that bypass JWT validation.
     * Not part of CommonSecurityEndpoints.PUBLIC — those cover swagger/actuator only.
     */
    private static final List<String> PUBLIC_AUTH_ENDPOINTS = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh"
    );

    /**
     * Service-specific public endpoints accessible without authentication.
     */
    private static final List<String> SERVICE_SPECIFIC_PUBLIC_ENDPOINTS = List.of(
            "/auth-docs/**",
            "/ticket-docs/**",
            "/analysis-docs/**",
            "/routing-docs/**",
            "/rag-docs/**",
            "/orchestration-docs/**",
            "/ws/**"
    );

    /**
     * Combined set of all public endpoint patterns checked on every request.
     */
    private static final List<String> ALL_PUBLIC_ENDPOINTS = Stream.of(
            PUBLIC_AUTH_ENDPOINTS.stream(),
            CommonSecurityEndpoints.PUBLIC.stream(),
            SERVICE_SPECIFIC_PUBLIC_ENDPOINTS.stream()
    ).flatMap(s -> s).toList();
    
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

        boolean isPublic = ALL_PUBLIC_ENDPOINTS.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));

        if (isPublic) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders()
                .getFirst(SecurityConstants.AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            
        	return onError(exchange,
            		"Missing or invalid Authorization header",
            		HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(SecurityConstants.BEARER_PREFIX.length());

        if (!jwtUtil.isTokenValid(token)) {
        	
            return onError(exchange,
            		"Invalid JWT token",
            		HttpStatus.UNAUTHORIZED);
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
                        .headers(headers -> {
                            headers.remove(SecurityConstants.HEADER_USER_ID);
                            headers.remove(SecurityConstants.HEADER_USER_ROLE);
                            headers.remove(SecurityConstants.HEADER_USER_EMAIL);
                            headers.remove(SecurityConstants.HEADER_USER_NAME);
                            headers.set(SecurityConstants.HEADER_USER_ID, jwtUtil.extractUserId(token));
                            headers.set(SecurityConstants.HEADER_USER_ROLE, jwtUtil.extractRole(token));
                            headers.set(SecurityConstants.HEADER_USER_EMAIL, jwtUtil.extractEmail(token));
                            
                            String name = jwtUtil.extractName(token);
                            if (name != null) {
                                headers.set(SecurityConstants.HEADER_USER_NAME, name);
                            }
                        }))
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
    	return Ordered.HIGHEST_PRECEDENCE; // Execute before routing
    }
}
