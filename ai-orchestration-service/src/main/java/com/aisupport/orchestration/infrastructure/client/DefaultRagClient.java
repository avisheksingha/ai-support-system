package com.aisupport.orchestration.infrastructure.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.aisupport.common.event.KnowledgeContext;
import com.aisupport.common.event.KnowledgeSource;
import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.infrastructure.client.exception.RagUnavailableException;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DefaultRagClient implements RagClient {

    private static final String DEFAULT_ANSWER = "No relevant knowledge found.";
    private static final String ANSWER_KEY = "answer";
    private static final String CONFIDENCE_KEY = "confidence";
    private static final String SOURCES_KEY = "sources";
    private static final String GENERATED_REPLY_KEY = "generatedReply";
    private static final String SOURCE_DOCUMENTS_KEY = "sourceDocuments";
    private static final String SIMILARITY_SCORE_KEY = "similarityScore";
    private static final String TITLE_KEY = "title";
    private static final String ID_KEY = "id";

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

            return Result.success(buildKnowledgeContext(response, ANSWER_KEY, CONFIDENCE_KEY, SOURCES_KEY));
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
                    
            return Result.success(buildKnowledgeContext(response, GENERATED_REPLY_KEY, SIMILARITY_SCORE_KEY, SOURCE_DOCUMENTS_KEY));
        } catch (Exception e) {
            log.error("Failed to get RAG response for ticketId={}", ticketId, e);
            return Result.failure("RAG Not Found: " + e.getMessage());
        }
    }

    /**
     * Constructs the KnowledgeContext from the API response map.
     */
    private KnowledgeContext buildKnowledgeContext(Map<String, Object> response, String answerKey, String confidenceKey, String sourcesKey) {
        String answer = extractString(response, answerKey, DEFAULT_ANSWER);
        Double confidence = extractDouble(response, confidenceKey);
        List<KnowledgeSource> sources = extractSources(response, sourcesKey);
        return new KnowledgeContext(answer, sources, confidence);
    }

    /**
     * Safely extracts a list of KnowledgeSource objects from the response map.
     */
    private List<KnowledgeSource> extractSources(Map<String, Object> response, String key) {
        if (response == null || !(response.get(key) instanceof java.util.List<?> list)) {
            return Collections.emptyList();
        }
        
        List<KnowledgeSource> sources = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                sources.add(buildKnowledgeSource(map));
            }
        }
        return sources;
    }

    /**
     * Maps a raw response item into a KnowledgeSource object.
     */
    private KnowledgeSource buildKnowledgeSource(Map<?, ?> map) {
        String id = extractString(map, ID_KEY, null);
        String title = extractString(map, TITLE_KEY, null);
        Double simScore = extractDouble(map, SIMILARITY_SCORE_KEY);
        return new KnowledgeSource(id, title, simScore);
    }

    /**
     * Safely extracts a String value from a map.
     */
    private String extractString(Map<?, ?> map, String key, String defaultValue) {
        if (map != null && map.get(key) != null) {
            return map.get(key).toString();
        }
        return defaultValue;
    }

    /**
     * Safely extracts a Double value from a map.
     */
    private Double extractDouble(Map<?, ?> map, String key) {
        if (map != null && map.get(key) instanceof Number n) {
            return n.doubleValue();
        }
        return null;
    }
}