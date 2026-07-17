package com.aisupport.orchestration.infrastructure.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.aisupport.common.enums.TicketPriority;
import com.aisupport.common.event.AnalysisResult;
import com.aisupport.common.event.RoutingDecision;
import com.aisupport.common.event.TicketAnalyzedEvent;
import com.aisupport.common.event.TicketRoutedEvent;
import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.infrastructure.client.exception.RoutingUnavailableException;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DefaultRoutingClient implements RoutingClient {

    private final RestClient restClient;

    public DefaultRoutingClient(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
                                @Value("${api.services.routing.url}") String routingServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(routingServiceUrl).build();
    }

    @Override
    @Timed(value = "routing.client.duration", description = "Time taken by Routing Service")
    public Result<RoutingDecision> route(Long ticketId, Object analysisResultObj) {
    	
        log.info("Calling routing-service internal API for ticketId={}", ticketId);
        
        try {
            AnalysisResult analysis = (AnalysisResult) analysisResultObj;
            
            TicketAnalyzedEvent request = TicketAnalyzedEvent.builder()
                .ticketId(ticketId)
                .intent(analysis.intent())
                .sentiment(analysis.sentiment())
                .urgency(analysis.urgency())
                .build();
                
            TicketRoutedEvent response = restClient.post()
                    .uri("/api/internal/routing/route")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(TicketRoutedEvent.class);

            if (response != null) {
                RoutingDecision decision = new RoutingDecision(
                    response.getAssignToTeam(),
                    response.getPriority(),
                    response.getSlaHours()
                );
                return Result.success(decision);
            }
            return Result.failure("Null response from routing service");
        } catch (Exception e) {
            log.error("Failed to route ticketId={}", ticketId, e);
            throw new RoutingUnavailableException("Routing Service Unavailable: " + e.getMessage(), e);
        }
    }

    @Override
    public Result<RoutingDecision> getRouting(Long ticketId) {
        try {
            ParameterizedTypeReference<Map<String, Object>> typeRef = 
                new ParameterizedTypeReference<>() {};
                
            Map<String, Object> response = restClient.get()
                    .uri("/api/internal/routing/ticket/" + ticketId)
                    .retrieve()
                    .body(typeRef);
                    
            if (response != null && response.containsKey("department")) {
                String department = (String) response.get("department");
                return Result.success(new RoutingDecision(department, TicketPriority.LOW, 24));
            }
            return Result.failure("Routing response missing department");
        } catch (Exception e) {
            log.error("Failed to get routing for ticketId={}", ticketId, e);
            return Result.failure("Routing Not Found: " + e.getMessage());
        }
    }
}
