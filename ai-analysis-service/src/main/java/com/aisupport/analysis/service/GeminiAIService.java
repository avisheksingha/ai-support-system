package com.aisupport.analysis.service;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.aisupport.analysis.config.GeminiConfig;
import com.aisupport.analysis.dto.GeminiRequest;
import com.aisupport.analysis.dto.GeminiResponse;
import com.aisupport.analysis.dto.ParsedAnalysis;
import com.aisupport.analysis.exception.AIAnalysisException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiAIService {
    
    private final WebClient geminiWebClient;
    private final GeminiConfig geminiConfig;
    private final ObjectMapper objectMapper;
    
    @Value("${gemini.temperature}")
    private Double temperature;
    
    @Value("${gemini.max-output-tokens}")
    private Integer maxOutputTokens;
    
    @Value("${gemini.top-p}")
    private Double topP;
    
    @Value("${gemini.top-k}")
    private Integer topK;
    
    public ParsedAnalysis analyzeTicket(String subject, String message) {
        try {
            log.info("Analyzing ticket with Gemini AI");
            
            String prompt = buildAnalysisPrompt(subject, message);
            GeminiRequest request = buildGeminiRequest(prompt);
            
            String rawResponse = callGeminiAPI(request);
            log.debug("Raw Gemini response: {}", rawResponse);
            
            ParsedAnalysis analysis = parseGeminiResponse(rawResponse);
            log.info("Analysis completed - Intent: {}, Urgency: {}", 
                    analysis.getIntent(), analysis.getUrgency());
            
            return analysis;
            
        } catch (Exception e) {
            log.error("Failed to analyze ticket with Gemini AI", e);
            throw new AIAnalysisException("Gemini AI analysis failed: " + e.getMessage(), e);
        }
    }
    
    private String buildAnalysisPrompt(String subject, String message) {
        return String.format("""
                Analyze this support ticket and provide a JSON response with the following structure:
                
                {
                    "intent": "TECHNICAL|BILLING|ACCOUNT|FEATURE_REQUEST|COMPLAINT|GENERAL",
                    "sentiment": "POSITIVE|NEUTRAL|NEGATIVE|VERY_NEGATIVE",
                    "urgency": "LOW|MEDIUM|HIGH|CRITICAL",
                    "confidence_score": 0.0-1.0,
                    "keywords": ["keyword1", "keyword2", "keyword3"],
                    "suggested_category": "brief category description"
                }
                
                Ticket Subject: %s
                Ticket Message: %s
                
                Urgency Guidelines:
                - CRITICAL: System down, data loss, security breach, complete service outage, legal threats
                - HIGH: Major feature broken, angry customer, payment processing issues, multiple users affected
                - MEDIUM: Partial functionality issues, moderate complaints, single user issues
                - LOW: General questions, feature requests, positive feedback, minor issues
                
                Intent Categories:
                - TECHNICAL: Bug reports, technical issues, system errors, integration problems
                - BILLING: Payment issues, invoices, pricing questions, subscription problems
                - ACCOUNT: Login issues, account access, profile management, permissions
                - FEATURE_REQUEST: New feature suggestions, enhancement requests
                - COMPLAINT: Dissatisfaction, service quality issues, negative feedback
                - GENERAL: General inquiries, information requests, how-to questions
                
                Respond ONLY with valid JSON. No additional text, no markdown formatting.
                """, subject, message);
    }
    
    private GeminiRequest buildGeminiRequest(String prompt) {
        GeminiRequest.Part part = GeminiRequest.Part.builder()
                .text(prompt)
                .build();
        
        GeminiRequest.Content content = GeminiRequest.Content.builder()
                .parts(List.of(part))
                .build();
        
        GeminiRequest.GenerationConfig config = GeminiRequest.GenerationConfig.builder()
                .temperature(temperature)
                .topK(topK)
                .topP(topP)
                .maxOutputTokens(maxOutputTokens)
                .build();
        
        List<GeminiRequest.SafetySetting> safetySettings = List.of(
            GeminiRequest.SafetySetting.builder()
                .category("HARM_CATEGORY_HATE_SPEECH")
                .threshold("BLOCK_MEDIUM_AND_ABOVE")
                .build(),
            GeminiRequest.SafetySetting.builder()
                .category("HARM_CATEGORY_HARASSMENT")
                .threshold("BLOCK_MEDIUM_AND_ABOVE")
                .build()
            // Add other categories as needed
        );
        
        return GeminiRequest.builder()
                .contents(List.of(content))
                .generationConfig(config)
                .safetySettings(safetySettings)
                .build();
    }
    
    private String callGeminiAPI(GeminiRequest request) {
        try {
            GeminiResponse response = geminiWebClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("key", geminiConfig.getApiKey())
                            .build())
                    .body(Mono.just(request), GeminiRequest.class)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
            
            if (response == null || response.getCandidates() == null || response.getCandidates().isEmpty()) {
                throw new AIAnalysisException("Empty response from Gemini API", null);
            }
            
            return response.getCandidates().get(0)
                    .getContent()
                    .getParts()
                    .get(0)
                    .getText();
            
        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            throw new AIAnalysisException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }
    
    private ParsedAnalysis parseGeminiResponse(String rawResponse) {
        try {
            // Clean the response - remove markdown code blocks if present
            String cleanedResponse = rawResponse.trim();
            if (cleanedResponse.startsWith("```json")) {
                cleanedResponse = cleanedResponse.substring(7);
            }
            if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse.substring(3);
            }
            if (cleanedResponse.endsWith("```")) {
                cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
            }
            cleanedResponse = cleanedResponse.trim();
            
            return objectMapper.readValue(cleanedResponse, ParsedAnalysis.class);
            
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", rawResponse, e);
            throw new AIAnalysisException("Failed to parse AI response", e);
        }
    }

}
