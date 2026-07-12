package com.aisupport.orchestration.infrastructure.client.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.infrastructure.client.RoutingClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RoutingClientImpl implements RoutingClient {

    private final RestClient restClient;

    public RoutingClientImpl(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("http://routing-service:8084").build();
    }

    @Override
    @CircuitBreaker(name = "routing")
    @Retry(name = "routing")
    public Result<Object> route(Long ticketId, Object analysisResult) {
        log.info("Calling routing-service internal API for ticketId={}", ticketId);
        try {
            Object response = restClient.post()
                    .uri("/api/internal/routing/route")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(analysisResult)
                    .retrieve()
                    .body(Object.class);

            return Result.success(response);
        } catch (Exception e) {
            log.error("Failed to route ticketId={}", ticketId, e);
            return Result.failure("Routing failed: " + e.getMessage());
        }
    }
}
