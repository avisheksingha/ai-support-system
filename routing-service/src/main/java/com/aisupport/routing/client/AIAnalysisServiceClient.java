package com.aisupport.routing.client;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.aisupport.common.dto.AnalysisResultDTO;
import com.aisupport.routing.exception.ServiceCommunicationException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AIAnalysisServiceClient {
    
    private final WebClient webClient;
    private final String baseUrl;
    
    public AIAnalysisServiceClient(WebClient.Builder webClientBuilder,
                                  @Value("${api.services.ai-analysis.url}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }
    
    public AnalysisResultDTO getAnalysisByTicketId(Long ticketId) {
        log.info("Fetching analysis from AI Analysis Service for ticket: {}", ticketId);
        
        try {
            return webClient.get()
                    .uri("/api/v1/analysis/ticket/{ticketId}", ticketId)
                    .retrieve()
                    .bodyToMono(AnalysisResultDTO.class)
                    .timeout(Duration.ofSeconds(30))
                    .doOnSuccess(analysis -> log.debug("Successfully fetched analysis for ticket: {}", ticketId))
                    .doOnError(error -> log.error("Error fetching analysis for ticket {}: {}", 
                            ticketId, error.getMessage()))
                    .block();
                    
        } catch (Exception e) {
            log.error("Failed to fetch analysis for ticket {}", ticketId, e);
            throw new ServiceCommunicationException(
                    "Failed to communicate with AI Analysis Service", e);
        }
    }
}