package com.aisupport.orchestration.infrastructure.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    private static final String DEFAULT_ANSWER = "No relevant knowledge article found.";
    private static final String ANSWER_KEY = "answer";
    private static final String KNOWLEDGE_FOUND_KEY = "knowledgeFound";
    private static final String MODEL_KEY = "model";
    
    private static final String GENERATED_REPLY_KEY = "generatedReply";
    private static final String MODEL_USED_KEY = "modelUsed";
    private static final String UNKNOWN_MODEL = "Unknown";
    private static final String RETRIEVED_DOCUMENT_COUNT_KEY = "retrievedDocumentCount";
    private static final String MATCHED_ARTICLE_TITLES_KEY = "matchedArticleTitles";

    private final RestClient restClient;

    public DefaultRagClient(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
                            @Value("${api.services.rag.url:http://RAG-SERVICE}") String ragServiceUrl) {
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

            return Result.success(buildKnowledgeContext(response));
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
                    
            return Result.success(buildKnowledgeContext(response));
        } catch (Exception e) {
            log.error("Failed to get RAG response for ticketId={}", ticketId, e);
            return Result.failure("RAG Not Found: " + e.getMessage());
        }
    }

    /**
     * Constructs the KnowledgeContext from the API response map.
     */
    private KnowledgeContext buildKnowledgeContext(Map<String, Object> response) {
        if (response == null) {
            return new KnowledgeContext(DEFAULT_ANSWER, false, UNKNOWN_MODEL, 0, Collections.emptyList());
        }

        // Handle both /search (answer) and /ticket/{id} (generatedReply) formats
        String answer = extractString(response, ANSWER_KEY, extractString(response, GENERATED_REPLY_KEY, DEFAULT_ANSWER));
        boolean knowledgeFound = extractBoolean(response, KNOWLEDGE_FOUND_KEY);
        String model = extractString(response, MODEL_KEY, extractString(response, MODEL_USED_KEY, UNKNOWN_MODEL));
        Integer retrievedDocumentCount = extractInteger(response, RETRIEVED_DOCUMENT_COUNT_KEY);
        
        List<String> matchedArticleTitles = Collections.emptyList();
        Object rawTitles = response.get(MATCHED_ARTICLE_TITLES_KEY);
        
        // Safely cast
        if (rawTitles instanceof List<?> titleList) {
            matchedArticleTitles = titleList.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .toList();
        }
        
        return new KnowledgeContext(answer, knowledgeFound, model, retrievedDocumentCount, matchedArticleTitles);
    }

    private Integer extractInteger(Map<?, ?> map, String key) {
        if (map != null && map.get(key) instanceof Number n) {
            return n.intValue();
        }
        return null;
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
     * Safely extracts a boolean value from a map.
     */
    private boolean extractBoolean(Map<?, ?> map, String key) {
        if (map != null && map.get(key) instanceof Boolean b) {
            return b;
        }
        return false;
    }
}