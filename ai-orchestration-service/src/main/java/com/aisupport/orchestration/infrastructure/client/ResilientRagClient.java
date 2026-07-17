package com.aisupport.orchestration.infrastructure.client;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.aisupport.common.event.KnowledgeContext;
import com.aisupport.orchestration.domain.model.Result;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;

@Primary
@Service
@Slf4j
public class ResilientRagClient implements RagClient {

    private final DefaultRagClient defaultRagClient;

    public ResilientRagClient(DefaultRagClient defaultRagClient) {
        this.defaultRagClient = defaultRagClient;
    }

    @Override
    @CircuitBreaker(name = "rag", fallbackMethod = "searchKnowledgeFallback")
    @Retry(name = "rag", fallbackMethod = "searchKnowledgeFallback")
    public Result<KnowledgeContext> searchKnowledge(Long ticketId, String query) {
        return defaultRagClient.searchKnowledge(ticketId, query);
    }

    public Result<KnowledgeContext> searchKnowledgeFallback(Long ticketId, String query, Throwable t) {
        log.warn("Resilience fallback triggered for RAG search on ticketId={} with query={}: {}", ticketId, query, t.getMessage());
        return Result.failure("RAG Service Unavailable (Resilience Fallback)");
    }

    @Override
    @CircuitBreaker(name = "rag", fallbackMethod = "getRagResponseFallback")
    @Retry(name = "rag", fallbackMethod = "getRagResponseFallback")
    public Result<KnowledgeContext> getRagResponse(Long ticketId) {
        return defaultRagClient.getRagResponse(ticketId);
    }

    public Result<KnowledgeContext> getRagResponseFallback(Long ticketId, Throwable t) {
        log.warn("Resilience fallback triggered for fetching RAG response on ticketId={}: {}", ticketId, t.getMessage());
        return Result.failure("RAG Service Unavailable (Resilience Fallback)");
    }
}
