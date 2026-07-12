package com.aisupport.orchestration.infrastructure.client.impl;

import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.infrastructure.client.RagClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RagClientImpl implements RagClient {

    private final RestClient restClient;

    public RagClientImpl(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("http://rag-service:8085").build();
    }

    @Override
    @CircuitBreaker(name = "rag")
    @Retry(name = "rag")
    public Result<List<Object>> searchKnowledge(String query) {
        log.info("Calling rag-service internal API for query={}", query);
        try {
            Map<String, Object> request = Map.of(
                "ticketId", 0L, // Dummy ID for pure knowledge search, or extract if needed
                "query", query
            );

            Map<String, Object> response = restClient.post()
                    .uri("/api/internal/rag/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            return Result.success(List.of(response.get("response")));
        } catch (Exception e) {
            log.error("Failed to search knowledge for query={}", query, e);
            return Result.failure("RAG search failed: " + e.getMessage());
        }
    }
}
