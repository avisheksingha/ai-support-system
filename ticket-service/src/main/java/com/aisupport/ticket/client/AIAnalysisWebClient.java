package com.aisupport.ticket.client;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.aisupport.common.dto.AnalysisResultDTO;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
public class AIAnalysisWebClient {

    private final WebClient webClient;
    private static final String ANALYSIS_ENDPOINT = "/api/v1/analysis/analyze";

    // Inject base URL from application.properties for flexibility
    public AIAnalysisWebClient(WebClient.Builder builder,
                               @Value("${api.services.ai-analysis.url}") String baseUrl) {
        this.webClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<AnalysisResultDTO> analyzeTicket(AnalysisRequest request) {
        return webClient.post()
                .uri(ANALYSIS_ENDPOINT)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AnalysisResultDTO.class)
                // Optional per-call timeout (global timeout already in ApplicationConfig)
                // .timeout(Duration.ofSeconds(5))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)))
                .onErrorResume(ex -> {
                    // Default fallback response in case of errors
                    AnalysisResultDTO fallback = AnalysisResultDTO.builder()
                            .ticketId(request.getTicketId())
                            .intent("UNKNOWN")
                            .sentiment("NEUTRAL")
                            .urgency("LOW")
                            .confidenceScore(BigDecimal.ZERO)
                            .suggestedCategory("Fallback")
                            .rawResponse("Service unavailable")
                            .analysisProvider("Fallback")
                            .analyzedAt(LocalDateTime.now())
                            .build();
                    return Mono.just(fallback);
                });
    }
}
