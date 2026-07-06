package com.aisupport.gateway.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import com.aisupport.common.auth.SecurityConstants;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import reactor.core.publisher.Mono;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        secretKey = Jwts.SIG.HS256.key().build();
        String secret = Encoders.BASE64.encode(secretKey.getEncoded());
        filter = new JwtAuthenticationFilter(secret, 120);
    }

    private String generateToken(int expireInSeconds) {
        return Jwts.builder()
                .subject("100")
                .claim("email", "test@test.com")
                .claim("role", "ROLE_CUSTOMER")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(expireInSeconds)))
                .signWith(secretKey)
                .compact();
    }

    private ServerWebExchange createExchange(String token) {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/tickets")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        return MockServerWebExchange.from(request);
    }

    @Test
    void shouldNotAddRefreshHeaderWhenTokenIsStillValid() {
        String token = generateToken(300); // 5 minutes

        ServerWebExchange exchange = createExchange(token);
        GatewayFilterChain filterChain = exchange1 -> Mono.empty();

        filter.filter(exchange, filterChain).block();

        assertNull(exchange.getResponse().getHeaders().getFirst(SecurityConstants.HEADER_ACCESS_TOKEN_REFRESH));
    }

    @Test
    void shouldAddRefreshHeaderWhenTokenIsNearExpiry() {
        String token = generateToken(90); // 90 seconds (below 120s threshold)

        ServerWebExchange exchange = createExchange(token);
        GatewayFilterChain filterChain = exchange1 -> Mono.empty();

        filter.filter(exchange, filterChain).block();

        String refreshHeader = exchange.getResponse().getHeaders().getFirst(SecurityConstants.HEADER_ACCESS_TOKEN_REFRESH);
        assertEquals("true", refreshHeader);
    }

    @Test
    void shouldAddRefreshHeaderWhenRemainingEqualsThreshold() {
        String token = generateToken(120); // exactly 120 seconds

        ServerWebExchange exchange = createExchange(token);
        GatewayFilterChain filterChain = exchange1 -> Mono.empty();

        filter.filter(exchange, filterChain).block();

        String refreshHeader = exchange.getResponse().getHeaders().getFirst(SecurityConstants.HEADER_ACCESS_TOKEN_REFRESH);
        assertEquals("true", refreshHeader);
    }

    @Test
    void shouldRejectInvalidToken() {
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.signature";

        ServerWebExchange exchange = createExchange(invalidToken);
        GatewayFilterChain filterChain = exchange1 -> Mono.empty();

        filter.filter(exchange, filterChain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertNull(exchange.getResponse().getHeaders().getFirst(SecurityConstants.HEADER_ACCESS_TOKEN_REFRESH));
    }

    @Test
    void shouldNotAuthenticateExpiredToken() {
        String token = generateToken(-5); // expired 5 seconds ago

        ServerWebExchange exchange = createExchange(token);
        GatewayFilterChain filterChain = exchange1 -> Mono.empty();

        filter.filter(exchange, filterChain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertNull(exchange.getResponse().getHeaders().getFirst(SecurityConstants.HEADER_ACCESS_TOKEN_REFRESH));
    }

    @Test
    void shouldReplaceSpoofedIdentityHeadersWithTokenClaims() {
        String token = generateToken(300);
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/tickets")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(SecurityConstants.HEADER_USER_ID, "attacker")
                .header(SecurityConstants.HEADER_USER_ROLE, "ROLE_ADMIN")
                .header(SecurityConstants.HEADER_USER_EMAIL, "attacker@example.com")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();

        filter.filter(exchange, forwardedExchange -> {
            forwarded.set(forwardedExchange);
            return Mono.empty();
        }).block();

        assertEquals("100", forwarded.get().getRequest().getHeaders()
                .getFirst(SecurityConstants.HEADER_USER_ID));
        assertEquals("ROLE_CUSTOMER", forwarded.get().getRequest().getHeaders()
                .getFirst(SecurityConstants.HEADER_USER_ROLE));
        assertEquals("test@test.com", forwarded.get().getRequest().getHeaders()
                .getFirst(SecurityConstants.HEADER_USER_EMAIL));
    }
}
