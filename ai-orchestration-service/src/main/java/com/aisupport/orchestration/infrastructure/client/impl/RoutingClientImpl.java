package com.aisupport.orchestration.infrastructure.client.impl;

import org.springframework.stereotype.Service;

import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.infrastructure.client.RoutingClient;

@Service
public class RoutingClientImpl implements RoutingClient {
    @Override
    public Result<Object> route(Long ticketId, Object analysisResult) {
        return Result.success("Routed to Team A");
    }
}
