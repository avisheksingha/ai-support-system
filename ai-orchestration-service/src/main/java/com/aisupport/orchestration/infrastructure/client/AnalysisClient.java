package com.aisupport.orchestration.infrastructure.client;

import com.aisupport.common.event.AnalysisResult;
import com.aisupport.orchestration.domain.model.Result;

public interface AnalysisClient {
    Result<AnalysisResult> analyze(Long ticketId, String content);
    Result<AnalysisResult> getAnalysis(Long ticketId);
}
