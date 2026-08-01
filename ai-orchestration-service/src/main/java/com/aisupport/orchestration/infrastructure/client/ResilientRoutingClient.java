package com.aisupport.orchestration.infrastructure.client;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.aisupport.common.event.RoutingDecision;
import com.aisupport.orchestration.domain.model.Result;

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
    public Result<RoutingDecision> route(Long ticketId, Object analysisResult) {
        return defaultRoutingClient.route(ticketId, analysisResult);
    }

    public Result<RoutingDecision> routeFallback(Long ticketId, Object analysisResult, Throwable t) {
        log.warn("Resilience fallback triggered for routing ticketId={} with analysisResult={}: {}", ticketId, analysisResult, t.getMessage());
        return Result.failure("Routing Service Unavailable (Resilience Fallback)");
    }

    @Override
    @CircuitBreaker(name = "routing", fallbackMethod = "getRoutingFallback")
    @Retry(name = "routing", fallbackMethod = "getRoutingFallback")
    public Result<RoutingDecision> getRouting(Long ticketId) {
        return defaultRoutingClient.getRouting(ticketId);
    }

    public Result<RoutingDecision> getRoutingFallback(Long ticketId, Throwable t) {
        log.warn("Resilience fallback triggered for fetching routing on ticketId={}: {}", ticketId, t.getMessage());
        return Result.failure("Routing Service Unavailable (Resilience Fallback)");
    }
}
