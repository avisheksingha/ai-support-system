package com.aisupport.routing.client;

import com.aisupport.routing.dto.RuleEvaluationRequest;
import com.aisupport.routing.dto.RuleEvaluationResponse;
import com.aisupport.routing.exception.ServiceCommunicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@Slf4j
public class RuleEngineServiceClient {
    
    private final WebClient webClient;
    private final String baseUrl;
    
    public RuleEngineServiceClient(WebClient.Builder webClientBuilder,
                                  @Value("${webclient.service.rule-engine-service}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }
    
    public RuleEvaluationResponse evaluateRules(RuleEvaluationRequest request) {
        log.info("Evaluating rules for ticket: {}", request.getTicketId());
        
        try {
            return webClient.post()
                    .uri("/api/v1/rules/evaluate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(RuleEvaluationResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .doOnSuccess(response -> log.debug("Successfully evaluated rules for ticket: {}", 
                            request.getTicketId()))
                    .doOnError(error -> log.error("Error evaluating rules for ticket {}: {}", 
                            request.getTicketId(), error.getMessage()))
                    .block();
                    
        } catch (Exception e) {
            log.error("Failed to evaluate rules for ticket {}", request.getTicketId(), e);
            throw new ServiceCommunicationException(
                    "Failed to communicate with Rule Engine Service", e);
        }
    }
}