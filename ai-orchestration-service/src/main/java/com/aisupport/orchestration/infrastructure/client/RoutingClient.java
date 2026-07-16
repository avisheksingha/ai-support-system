package com.aisupport.orchestration.infrastructure.client;

import com.aisupport.orchestration.domain.model.Result;

public interface RoutingClient {
    Result<Object> route(Long ticketId, Object analysisResult); // Placeholder signature
    Result<Object> getRouting(Long ticketId);
}
