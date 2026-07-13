package com.aisupport.orchestration.infrastructure.client.impl;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.infrastructure.client.RoutingClient;
import com.aisupport.orchestration.infrastructure.client.exception.RoutingUnavailableException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;

@Primary
@Service
@Slf4j
public class ResilientRoutingClient implements RoutingClient {

    private final DefaultRoutingClient defaultRoutingClient;

    public ResilientRoutingClient(DefaultRoutingClient defaultRoutingClient) {
        this.defaultRoutingClient = defaultRoutingClient;
    }

    @Override
    @CircuitBreaker(name = "routing", fallbackMethod = "routeFallback")
    @Retry(name = "routing", fallbackMethod = "routeFallback")
    public Result<Object> route(Long ticketId, Object analysisResult) {
        return defaultRoutingClient.route(ticketId, analysisResult);
    }

    public Result<Object> routeFallback(Long ticketId, Object analysisResult, Throwable t) {
        log.warn("Resilience fallback triggered for routing on ticketId={}: {}", ticketId, t.getMessage());
        throw new RoutingUnavailableException("Routing Service Unavailable (Resilience Fallback)", t);
    }
}
