package com.aisupport.orchestration.infrastructure.client;

import java.util.List;

import com.aisupport.orchestration.domain.model.Result;

public interface RagClient {
    Result<List<Object>> searchKnowledge(String query); // Placeholder signature
}
