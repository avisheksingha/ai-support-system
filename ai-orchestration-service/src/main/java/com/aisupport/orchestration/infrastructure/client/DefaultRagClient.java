package com.aisupport.orchestration.infrastructure.client;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.aisupport.common.event.KnowledgeContext;
import com.aisupport.orchestration.domain.model.Result;
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
    public Result<KnowledgeContext> searchKnowledge(Long ticketId, String query) {
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

            String answer = response != null && response.containsKey("answer") ? (String) response.get("answer") : "No relevant knowledge found.";
            
            KnowledgeContext context = new KnowledgeContext(answer, Collections.emptyList(), 1.0);
            return Result.success(context);
        } catch (Exception e) {
            log.error("Failed to search knowledge for query={}", query, e);
            throw new RagUnavailableException("RAG Service Unavailable: " + e.getMessage(), e);
        }
    }

    @Override
    public Result<KnowledgeContext> getRagResponse(Long ticketId) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri("/api/internal/rag/ticket/" + ticketId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
                    
            String answer = response != null && response.containsKey("generatedReply") ? (String) response.get("generatedReply") : "No relevant knowledge found.";
            KnowledgeContext context = new KnowledgeContext(answer, Collections.emptyList(), 1.0);
            return Result.success(context);
        } catch (Exception e) {
            log.error("Failed to get RAG response for ticketId={}", ticketId, e);
            return Result.failure("RAG Not Found: " + e.getMessage());
        }
    }
}
