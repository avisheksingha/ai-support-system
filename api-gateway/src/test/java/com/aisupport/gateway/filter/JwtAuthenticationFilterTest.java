package com.aisupport.gateway.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import com.aisupport.common.auth.SecurityConstants;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private final String secret = "4b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b";

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(secret, 120);
    }

    private String generateToken(int expireInSeconds) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .subject("100")
                .claim("email", "test@test.com")
                .claim("role", "ROLE_CUSTOMER")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(expireInSeconds)))
                .signWith(key)
                .compact();
    }

    @Test
    void testNoRefreshHeaderIfRemainingIsAboveThreshold() {
        String token = generateToken(300); // 5 minutes

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/tickets")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain filterChain = exchange1 -> Mono.empty();

        filter.filter(exchange, filterChain).block();

        assertTrue(exchange.getResponse().getHeaders().getFirst(SecurityConstants.HEADER_ACCESS_TOKEN_REFRESH) == null);
    }

    @Test
    void testRefreshHeaderAddedIfRemainingIsBelowThreshold() {
        String token = generateToken(90); // 90 seconds (below 120s threshold)

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/tickets")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain filterChain = exchange1 -> Mono.empty();

        filter.filter(exchange, filterChain).block();

        assertTrue(exchange.getResponse().getHeaders().getFirst(SecurityConstants.HEADER_ACCESS_TOKEN_REFRESH) != null);
        assertTrue(exchange.getResponse().getHeaders().getFirst(SecurityConstants.HEADER_ACCESS_TOKEN_REFRESH).contains("true"));
    }
}
