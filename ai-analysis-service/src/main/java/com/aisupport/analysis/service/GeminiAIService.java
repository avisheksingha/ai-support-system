package com.aisupport.analysis.service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.aisupport.analysis.config.GeminiProperties;
import com.aisupport.analysis.dto.GeminiRequest;
import com.aisupport.analysis.dto.ParsedAnalysis;
import com.aisupport.analysis.exception.AIAnalysisException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.util.retry.Retry;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiAIService {

    private final WebClient geminiWebClient;
    private final GeminiProperties props;
    private final ObjectMapper objectMapper;

    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
    private static final String BLOCK_MEDIUM_AND_ABOVE = "BLOCK_MEDIUM_AND_ABOVE";
    private static final List<GeminiRequest.SafetySetting> SAFETY_SETTINGS = List.of(
            GeminiRequest.SafetySetting.builder()
                    .category("HARM_CATEGORY_HATE_SPEECH")
                    .threshold(BLOCK_MEDIUM_AND_ABOVE)
                    .build(),
            GeminiRequest.SafetySetting.builder()
                    .category("HARM_CATEGORY_HARASSMENT")
                    .threshold(BLOCK_MEDIUM_AND_ABOVE)
                    .build(),
            GeminiRequest.SafetySetting.builder()
                    .category("HARM_CATEGORY_DANGEROUS_CONTENT")
                    .threshold(BLOCK_MEDIUM_AND_ABOVE)
                    .build(),
            GeminiRequest.SafetySetting.builder()
                    .category("HARM_CATEGORY_SEXUALLY_EXPLICIT")
                    .threshold(BLOCK_MEDIUM_AND_ABOVE)
                    .build()
    );

    /**
     * Analyze a support ticket using Gemini AI. This method builds the prompt, calls the Gemini API,
     * and parses the response into a structured ParsedAnalysis object.
     * 
     * @param subject
     * @param message
     * @return
     */
    public ParsedAnalysis analyzeTicket(String subject, String message) {
        String prompt = buildAnalysisPrompt(subject, message);
        return analyzeWithPrompt(prompt);
    }

    /**
	 * Core method that handles the entire flow of building the Gemini request,
	 * calling the API, and parsing the response.
	 * It includes robust error handling to catch and log any issues that arise during the process.
	 * 
	 * @param prompt
	 * @return
	 */
    private ParsedAnalysis analyzeWithPrompt(String prompt) {
        try {
            log.debug("Building Gemini request");
            GeminiRequest request = buildGeminiRequest(prompt);

            log.debug("Calling Gemini API");
            String rawResponse = callGeminiAPI(request);
            log.debug("Raw Gemini response: {}", rawResponse);

            ParsedAnalysis parsed = parseGeminiResponse(rawResponse);
            log.info("Parsed analysis: intent={}, urgency={}, confidence={}",
                    parsed.getIntent(), parsed.getUrgency(), parsed.getConfidenceScore());

            return parsed;

        } catch (AIAnalysisException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during AI analysis", e);
            throw new AIAnalysisException("AI analysis failed: " + e.getMessage(), e);
        }
    }

    /**
	 * Build the GeminiRequest object using the provided prompt and the configuration properties.
	 * This method constructs the request in the format expected by the Gemini API,
	 * including the generation configuration and safety settings.
	 * 
	 * @param prompt
	 * @return
	 */
    private GeminiRequest buildGeminiRequest(String prompt) {
        GeminiRequest.Part part = GeminiRequest.Part.builder()
                .text(prompt)
                .build();

        GeminiRequest.Content content = GeminiRequest.Content.builder()
                .parts(List.of(part))
                .build();

        GeminiRequest.GenerationConfig config = GeminiRequest.GenerationConfig.builder()
                .temperature(props.getTemperature())
                .topK(props.getTopK())
                .topP(props.getTopP())
                .maxOutputTokens(props.getMaxOutputTokens())
                .build();

        return GeminiRequest.builder()
                .contents(List.of(content))
                .generationConfig(config)
                .safetySettings(SAFETY_SETTINGS)
                .build();
    }

    /**
     * Call the Gemini API with the given request and return the raw response as a string.
     * This method uses WebClient to make a POST request to the Gemini endpoint, including the API key as a query parameter.
     * It includes error handling to catch specific HTTP errors (like 503 Service Unavailable) and timeouts, providing fallbacks where appropriate.
     * 
     * @param request
     * @return
     */
    private String callGeminiAPI(GeminiRequest request) {
        try {
            return geminiWebClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("key", props.getApiKey())
                            .build())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(props.getResponseTimeoutMs()))
                    .retryWhen(
                        Retry.backoff(3, Duration.ofSeconds(2))
                            .filter(ex -> ex instanceof WebClientResponseException webEx &&
                                          webEx.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE
                                       || ex instanceof TimeoutException)
                            .onRetryExhaustedThrow((retrySpec, signal) -> signal.failure())
                    )
                    .onErrorReturn(TimeoutException.class, "{}") // fallback for timeout
                    .block(Duration.ofMillis(props.getResponseTimeoutMs() + 2000L));

        } catch (WebClientResponseException webEx) {
            if (webEx.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                log.warn("Gemini API unavailable (503), returning empty analysis");
                return "{}"; // safe fallback
            }
            log.error("Error calling Gemini API", webEx);
            throw new AIAnalysisException("Failed to call Gemini API: " + webEx.getMessage(), webEx);
        } catch (Exception e) {
            log.error("Unexpected error calling Gemini API", e);
            throw new AIAnalysisException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }
    
    /**
	 * Parse the raw response from Gemini to extract the relevant analysis information. This method handles the nested structure of the Gemini response, extracts the inner JSON, and maps it to the ParsedAnalysis class.
	 * It includes error handling to catch any issues that arise during parsing, such as missing fields or invalid JSON, and logs the details for troubleshooting.
	 * 
	 * @param rawResponse
	 * @return
	 */
    private ParsedAnalysis parseGeminiResponse(String rawResponse) {
        try {
            // Step 1: Parse the outer Gemini response
            JsonNode root = objectMapper.readTree(rawResponse);

            // Navigate to candidates[0].content.parts[0].text
            JsonNode candidates = root.path("candidates");
            if (candidates.isMissingNode() || !candidates.isArray() || candidates.isEmpty()) {
                log.warn("No candidates in Gemini response");
                return new ParsedAnalysis();
            }

            JsonNode textNode = candidates.get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            if (textNode.isMissingNode() || !textNode.isTextual()) {
                log.warn("No text field in Gemini response");
                return new ParsedAnalysis();
            }

            String innerJson = textNode.asText();
            log.debug("Extracted inner JSON: {}", innerJson);

            // Step 2: Clean and parse the inner JSON
            String cleaned = cleanJsonResponse(innerJson);
            if ("{}".equals(cleaned.trim())) {
                log.warn("Model returned empty JSON fallback");
                return new ParsedAnalysis();
            }

            ParsedAnalysis parsed = objectMapper.readValue(cleaned, ParsedAnalysis.class);
            normalizeParsedAnalysis(parsed);
            return parsed;

        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", rawResponse, e);
            throw new AIAnalysisException("Failed to parse AI response", e);
        }
    }


    /**
	 * Normalize the parsed analysis by trimming and uppercasing string fields, and ensuring the confidence score is between 0.0 and 1.0.
	 * This method ensures that the ParsedAnalysis object has consistent formatting and valid values, which can help downstream processing and categorization.
	 * It also ensures that the keywords list is never null, defaulting to an empty list if not provided.
	 * 
	 * @param parsed
	 */
    private void normalizeParsedAnalysis(ParsedAnalysis parsed) {
        if (parsed == null) return;
        if (parsed.getIntent() != null) parsed.setIntent(parsed.getIntent().trim().toUpperCase());
        if (parsed.getSentiment() != null) parsed.setSentiment(parsed.getSentiment().trim().toUpperCase());
        if (parsed.getUrgency() != null) parsed.setUrgency(parsed.getUrgency().trim().toUpperCase());
        if (parsed.getConfidenceScore() != null) {
            double v = parsed.getConfidenceScore();
            if (Double.isNaN(v) || Double.isInfinite(v)) parsed.setConfidenceScore(0.0);
            else if (v < 0.0) parsed.setConfidenceScore(0.0);
            else if (v > 1.0) parsed.setConfidenceScore(1.0);
        }
        if (parsed.getKeywords() == null) parsed.setKeywords(List.of());
    }

    /**
     * Clean the inner JSON response from Gemini by removing markdown fences and extracting the first JSON object. This method handles cases where the model may include markdown formatting or additional text around the JSON, ensuring that we can still extract a valid JSON object for parsing.
     * 
     * @param response
     * @return
     */
    private String cleanJsonResponse(String response) {
        if (!StringUtils.hasText(response)) {
            return "{}";
        }

        String cleaned = response.trim();

        // Remove markdown fences anywhere in the string
        cleaned = cleaned.replaceAll("(?s)```json", "")
                         .replaceAll("(?s)```", "")
                         .trim();

        // Extract first JSON object
        Matcher matcher = JSON_BLOCK_PATTERN.matcher(cleaned);
        if (matcher.find()) {
            return matcher.group();
        }

        return cleaned;
    }

    /**
	 * Build the prompt for the Gemini model using the ticket subject and message. This method constructs a detailed prompt that instructs the model to respond with a specific JSON format, and provides context about the ticket.
	 * The prompt includes clear instructions on the expected output format, as well as examples of how to categorize urgency and intent. This helps guide the model towards producing consistent and structured responses.
	 * 
	 * @param subject
	 * @param message
	 * @return
	 */
    private String buildAnalysisPrompt(String subject, String message) {
        return """
            You are an AI support ticket analyzer.

            Your ONLY task: output a single valid JSON object.
            Do not include explanations, markdown fences, or any text outside the JSON object.

            The JSON must follow this schema exactly:
            {
              "intent": "TECHNICAL|BILLING|ACCOUNT|FEATURE_REQUEST|COMPLAINT|GENERAL",
              "sentiment": "POSITIVE|NEUTRAL|NEGATIVE|VERY_NEGATIVE",
              "urgency": "LOW|MEDIUM|HIGH|CRITICAL",
              "confidence_score": 0.0-1.0,
              "keywords": ["keyword1", "keyword2", "keyword3"],
              "suggested_category": "brief category description"
            }

            Ticket Context:
            Subject: %s
            Message: %s

            If you cannot produce valid JSON, output {} only.
            """.formatted(subject, message);
    }

}