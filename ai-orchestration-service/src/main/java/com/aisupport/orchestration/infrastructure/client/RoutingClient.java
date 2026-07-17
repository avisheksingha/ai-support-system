package com.aisupport.orchestration.infrastructure.client;

import com.aisupport.common.event.RoutingDecision;
import com.aisupport.orchestration.domain.model.Result;

public interface RoutingClient {
    Result<RoutingDecision> route(Long ticketId, Object analysisResult);
    Result<RoutingDecision> getRouting(Long ticketId);
}
