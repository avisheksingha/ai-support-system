package com.aisupport.orchestration.infrastructure.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.infrastructure.client.exception.AnalysisUnavailableException;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DefaultAnalysisClient implements AnalysisClient {

    private final RestClient restClient;

    public DefaultAnalysisClient(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
                                 @Value("${api.services.ai-analysis.url}") String analysisServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(analysisServiceUrl).build();
    }

    @Override
    @Timed(value = "analysis.client.duration", description = "Time taken by Analysis Service")
    public Result<Object> analyze(Long ticketId, String content) {
        log.info("Calling ai-analysis-service internal API for ticketId={}", ticketId);
        try {
            Map<String, Object> request = Map.of(
                "ticketId", ticketId,
                "subject", "Automated orchestration analysis",
                "message", content
            );

            Object response = restClient.post()
                    .uri("/api/internal/analysis/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(Object.class);

            return Result.success(response);
        } catch (Exception e) {
            log.error("Failed to analyze ticketId={}", ticketId, e);
            throw new AnalysisUnavailableException("Analysis Service Unavailable: " + e.getMessage(), e);
        }
    }
        
    @Override
    public Result<Object> getAnalysis(Long ticketId) {
        try {
            Object response = restClient.get()
                    .uri("/api/internal/analysis/ticket/" + ticketId)
                    .retrieve()
                    .body(Object.class);
            return Result.success(response);
        } catch (Exception e) {
            log.error("Failed to fetch analysis for ticketId={}", ticketId, e);
            return Result.failure("Analysis not found or unavailable: " + e.getMessage());
        }
    }
}
