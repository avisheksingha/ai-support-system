package com.aisupport.orchestration.infrastructure.client;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.infrastructure.client.exception.RagUnavailableException;

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
    @CircuitBreaker(name = "rag", fallbackMethod = "searchFallback")
    @Retry(name = "rag", fallbackMethod = "searchFallback")
    public Result<List<Object>> searchKnowledge(Long ticketId, String query) {
        return defaultRagClient.searchKnowledge(ticketId, query);
    }

    public Result<List<Object>> searchFallback(Long ticketId, String query, Throwable t) {
        log.warn("Resilience fallback triggered for rag search ticketId={} query={}: {}", ticketId, query, t.getMessage());
        throw new RagUnavailableException("RAG Service Unavailable (Resilience Fallback)", t);
    }

    @Override
    @CircuitBreaker(name = "rag", fallbackMethod = "getRagResponseFallback")
    @Retry(name = "rag", fallbackMethod = "getRagResponseFallback")
    public Result<Object> getRagResponse(Long ticketId) {
        return defaultRagClient.getRagResponse(ticketId);
    }

    public Result<Object> getRagResponseFallback(Long ticketId, Throwable t) {
        log.warn("Resilience fallback triggered for get rag response ticketId={}: {}", ticketId, t.getMessage());
        return Result.failure("RAG Not Found (Resilience Fallback): " + t.getMessage());
    }
}
