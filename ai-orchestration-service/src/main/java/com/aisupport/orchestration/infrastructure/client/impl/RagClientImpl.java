package com.aisupport.orchestration.infrastructure.client.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.infrastructure.client.RagClient;

@Service
public class RagClientImpl implements RagClient {
    @Override
    public Result<List<Object>> searchKnowledge(String query) {
        return Result.success(List.of("Dummy knowledge article"));
    }
}
