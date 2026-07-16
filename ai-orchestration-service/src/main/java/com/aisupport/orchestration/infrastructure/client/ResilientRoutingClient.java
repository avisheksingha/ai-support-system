package com.aisupport.orchestration.infrastructure.client;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.aisupport.orchestration.domain.model.Result;
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
        String analysisResultType = analysisResult == null ? "null" : analysisResult.getClass().getSimpleName();
        log.warn("Resilience fallback triggered for routing on ticketId={}, analysisResultType={}: {}",
                ticketId, analysisResultType, t.getMessage());
        throw new RoutingUnavailableException("Routing Service Unavailable (Resilience Fallback)", t);
    }

    @Override
    @CircuitBreaker(name = "routing", fallbackMethod = "getRoutingFallback")
    @Retry(name = "routing", fallbackMethod = "getRoutingFallback")
    public Result<Object> getRouting(Long ticketId) {
        return defaultRoutingClient.getRouting(ticketId);
    }

    public Result<Object> getRoutingFallback(Long ticketId, Throwable t) {
        log.warn("Resilience fallback triggered for get routing ticketId={}: {}", ticketId, t.getMessage());
        return Result.failure("Routing Not Found (Resilience Fallback): " + t.getMessage());
    }
}
