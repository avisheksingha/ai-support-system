package com.aisupport.orchestration.infrastructure.client.impl;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.infrastructure.client.RagClient;
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
    public Result<List<Object>> searchKnowledge(String query) {
        return defaultRagClient.searchKnowledge(query);
    }

    public Result<List<Object>> searchFallback(String query, Throwable t) {
        log.warn("Resilience fallback triggered for rag search query={}: {}", query, t.getMessage());
        throw new RagUnavailableException("RAG Service Unavailable (Resilience Fallback)", t);
    }
}
