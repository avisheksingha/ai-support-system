package com.aisupport.orchestration.infrastructure.client;

import com.aisupport.orchestration.domain.model.Result;

public interface AnalysisClient {
    Result<Object> analyze(Long ticketId, String content); // Placeholder signature
    Result<Object> getAnalysis(Long ticketId);
}
