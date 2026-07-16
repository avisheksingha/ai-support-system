package com.aisupport.orchestration.infrastructure.client.impl;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.infrastructure.client.AnalysisClient;
import com.aisupport.orchestration.infrastructure.client.exception.AnalysisUnavailableException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;

@Primary
@Service
@Slf4j
public class ResilientAnalysisClient implements AnalysisClient {

    private final DefaultAnalysisClient defaultAnalysisClient;

    public ResilientAnalysisClient(DefaultAnalysisClient defaultAnalysisClient) {
        this.defaultAnalysisClient = defaultAnalysisClient;
    }

    @Override
    @CircuitBreaker(name = "analysis", fallbackMethod = "analyzeFallback")
    @Retry(name = "analysis", fallbackMethod = "analyzeFallback")
    public Result<Object> analyze(Long ticketId, String content) {
        return defaultAnalysisClient.analyze(ticketId, content);
    }

    public Result<Object> analyzeFallback(Long ticketId, String content, Throwable t) {
        int contentLength = content == null ? 0 : content.length();
        log.warn("Resilience fallback triggered for analysis on ticketId={}, contentLength={}: {}",
                ticketId, contentLength, t.getMessage());
        throw new AnalysisUnavailableException("Analysis Service Unavailable (Resilience Fallback)", t);
    }

    @Override
    @CircuitBreaker(name = "analysis", fallbackMethod = "getAnalysisFallback")
    @Retry(name = "analysis", fallbackMethod = "getAnalysisFallback")
    public Result<Object> getAnalysis(Long ticketId) {
        return defaultAnalysisClient.getAnalysis(ticketId);
    }

    public Result<Object> getAnalysisFallback(Long ticketId, Throwable t) {
        log.warn("Resilience fallback triggered for fetching analysis on ticketId={}: {}",
                ticketId, t.getMessage());
        return Result.failure("Analysis Service Unavailable (Resilience Fallback)");
    }
}
