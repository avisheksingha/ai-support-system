package com.aisupport.orchestration.infrastructure.client.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.infrastructure.client.RagClient;
import com.aisupport.orchestration.infrastructure.client.exception.RagUnavailableException;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DefaultRagClient implements RagClient {

    private final RestClient restClient;

    public DefaultRagClient(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
                            @Value("${api.services.rag.url}") String ragServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(ragServiceUrl).build();
    }

    @Override
    @Timed(value = "rag.client.duration", description = "Time taken by RAG Service")
    public Result<List<Object>> searchKnowledge(Long ticketId, String query) {
        log.info("Calling rag-service internal API for ticketId={} query={}", ticketId, query);
        try {
            Map<String, Object> request = Map.of(
                "ticketId", ticketId,
                "query", query
            );

            Map<String, Object> response = restClient.post()
                    .uri("/api/internal/rag/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            return Result.success(List.of(response.get("answer")));
        } catch (Exception e) {
            log.error("Failed to search knowledge for query={}", query, e);
            throw new RagUnavailableException("RAG Service Unavailable: " + e.getMessage(), e);
        }
    }

    @Override
    public Result<Object> getRagResponse(Long ticketId) {
        try {
            Object response = restClient.get()
                    .uri("/api/internal/rag/ticket/" + ticketId)
                    .retrieve()
                    .body(Object.class);
            return Result.success(response);
        } catch (Exception e) {
            log.error("Failed to get RAG response for ticketId={}", ticketId, e);
            return Result.failure("RAG Not Found: " + e.getMessage());
        }
    }
}
