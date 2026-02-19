package com.aisupport.ticket.client;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.aisupport.common.dto.AnalysisResultDTO;
import com.aisupport.ticket.config.AIAnalysisServicePropertiesConfig;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@Component
public class AIAnalysisServiceClient {

    private final WebClient webClient;
    private static final String ANALYSIS_ENDPOINT = "/api/v1/analysis/analyze";
    private static final String UNKNOWN = "UNKNOWN";
    private static final String NEUTRAL = "NEUTRAL";
    private static final String LOW = "LOW";
    private static final String FALLBACK = "Fallback";

    // Inject base URL from application.properties for flexibility
    public AIAnalysisServiceClient(WebClient.Builder builder, AIAnalysisServicePropertiesConfig properties) {
        this.webClient = builder
            .baseUrl(properties.getUrl())
            .build();
    }

    /**
	 * Calls the AI analysis service to analyze a support ticket.
	 * Uses Resilience4j's Circuit Breaker to handle failures gracefully.
	 * Retries up to 3 times with exponential backoff if the call fails.
	 * If all retries fail, it falls back to a default response.
	 * 
	 * @param request The analysis request containing ticket details.
	 * @return A Mono emitting the analysis result or a fallback response in case of failure.	 * 
	 */
    @CircuitBreaker(name = "aiAnalysisService", fallbackMethod = "fallbackAnalysis")
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

    /**
	 * Fallback method for AI analysis failures. Logs the error and returns a default response.
	 * This method is called by Resilience4j's Circuit Breaker when the main method fails.
	 *
	 * @param request The original analysis request that failed.
	 * @param ex The exception that caused the failure.
	 * @return A Mono emitting a fallback AnalysisResultDTO with default values.
	 */
    private Mono<AnalysisResultDTO> fallbackAnalysis(AnalysisRequest request, Throwable ex) {
        log.error("AI analysis failed for request: {}", request, ex);
        return Mono.just(createFallbackResponse(request));
    }
    
    /**
	 * Creates a fallback AnalysisResultDTO with default values indicating the service is unavailable.
	 * This method is used to generate a response when the AI analysis service cannot be reached or fails.
	 * 
	 * @param request The original analysis request for which the fallback response is being created.
	 * @return An AnalysisResultDTO with default values indicating a fallback response.
	 */
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
