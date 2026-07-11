package com.aisupport.orchestration.infrastructure.client.impl;

import org.springframework.stereotype.Service;

import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.infrastructure.client.AnalysisClient;

@Service
public class AnalysisClientImpl implements AnalysisClient {
    @Override
    public Result<Object> analyze(Long ticketId, String content) {
        return Result.success("Dummy analysis result");
    }
}
