package com.aisupport.orchestration.infrastructure.client.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import java.util.Map;

import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.infrastructure.client.AnalysisClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AnalysisClientImpl implements AnalysisClient {

    private final RestClient restClient;

    public AnalysisClientImpl(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("http://ai-analysis-service:8083").build();
    }

    @Override
    @CircuitBreaker(name = "analysis")
    @Retry(name = "analysis")
    public Result<Object> analyze(Long ticketId, String content) {
        log.info("Calling ai-analysis-service internal API for ticketId={}", ticketId);
        try {
            Map<String, Object> request = Map.of(
                "ticketId", ticketId,
                "subject", "Automated orchestration analysis",
                "message", content
            );

            Object response = restClient.post()
                    .uri("/api/internal/analysis/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(Object.class);

            return Result.success(response);
        } catch (Exception e) {
            log.error("Failed to analyze ticketId={}", ticketId, e);
            return Result.failure("Analysis failed: " + e.getMessage());
        }
    }
}
