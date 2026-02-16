package com.aisupport.ticket.client;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import com.aisupport.ticket.config.AIAnalysisProperties;

import com.aisupport.common.dto.AnalysisResultDTO;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Slf4j
@Component
public class AIAnalysisWebClient {

    private final WebClient webClient;
    private static final String ANALYSIS_ENDPOINT = "/api/v1/analysis/analyze";
    private static final String UNKNOWN = "UNKNOWN";
    private static final String NEUTRAL = "NEUTRAL";
    private static final String LOW = "LOW";
    private static final String FALLBACK = "Fallback";

    // Inject base URL from application.properties for flexibility
    public AIAnalysisWebClient(WebClient.Builder builder, AIAnalysisProperties properties) {
        this.webClient = builder
            .baseUrl(properties.getUrl())
            .build();
    }

    @CircuitBreaker(name = "aiAnalysis", fallbackMethod = "fallbackAnalysis")
    public Mono<AnalysisResultDTO> analyzeTicket(AnalysisRequest request) {
        return webClient.post()
            .uri(ANALYSIS_ENDPOINT)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(AnalysisResultDTO.class)
            // Optional per-call timeout (global timeout already in WebClientConfig)
            // .timeout(Duration.ofSeconds(5))
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
            	.doBeforeRetry(retrySignal -> {
                	log.warn("Retrying AI analysis due to error: {}", retrySignal.failure().getMessage());
                	log.error("Retry attempt #{} for request {}", retrySignal.totalRetries(), request);
            	})
            )
            .onErrorResume(ex -> fallbackAnalysis(request, ex));
    }

    private Mono<AnalysisResultDTO> fallbackAnalysis(AnalysisRequest request, Throwable ex) {
        log.error("AI analysis failed for request: {}", request, ex);
        return Mono.just(createFallbackResponse(request));
    }

    private AnalysisResultDTO createFallbackResponse(AnalysisRequest request) {
        return AnalysisResultDTO.builder()
            .ticketId(request.getTicketId())
            .intent(UNKNOWN)
            .sentiment(NEUTRAL)
            .urgency(LOW)
            .confidenceScore(BigDecimal.ZERO)
            .suggestedCategory(FALLBACK)
            .rawResponse("Service unavailable (circuit breaker)")
            .analysisProvider(FALLBACK)
            .analyzedAt(LocalDateTime.now())
            .build();
    }
}
