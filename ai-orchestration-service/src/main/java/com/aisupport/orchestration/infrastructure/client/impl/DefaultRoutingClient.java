package com.aisupport.orchestration.infrastructure.client.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.infrastructure.client.RoutingClient;
import com.aisupport.orchestration.infrastructure.client.exception.RoutingUnavailableException;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DefaultRoutingClient implements RoutingClient {

    private final RestClient restClient;

    public DefaultRoutingClient(RestClient.Builder restClientBuilder,
                                @Value("${api.services.routing.url}") String routingServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(routingServiceUrl).build();
    }

    @Override
    @Timed(value = "routing.client.duration", description = "Time taken by Routing Service")
    public Result<Object> route(Long ticketId, Object analysisResult) {
        log.info("Calling routing-service internal API for ticketId={}", ticketId);
        try {
            Object response = restClient.post()
                    .uri("/api/internal/routing/route")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(analysisResult)
                    .retrieve()
                    .body(Object.class);

            return Result.success(response);
        } catch (Exception e) {
            log.error("Failed to route ticketId={}", ticketId, e);
            throw new RoutingUnavailableException("Routing Service Unavailable: " + e.getMessage(), e);
        }
    }
}
