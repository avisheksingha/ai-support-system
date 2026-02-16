package com.aisupport.analysis.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for Gemini AI")
public class GeminiRequest {
    
    @Schema(description = "List of contents")
    private List<Content> contents;
    
    @JsonProperty("generationConfig")
    @Schema(description = "Generation configuration")
    private GenerationConfig generationConfig;
    
    @JsonProperty("safetySettings")
    @Schema(description = "Safety settings")
    private List<SafetySetting> safetySettings;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Content of the request")
    public static class Content {
        @Schema(description = "List of parts")
        private List<Part> parts;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Part of the request")
    public static class Part {
        @Schema(description = "Text of the part", example = "Hello, I need help with my account.")
        private String text;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Generation configuration")
    public static class GenerationConfig {
        @Schema(description = "Temperature of the generation", example = "0.7")
        private Double temperature;
        
        @JsonProperty("topK")
        @Schema(description = "Top K of the generation", example = "40")
        private Integer topK;
        
        @JsonProperty("topP")
        @Schema(description = "Top P of the generation", example = "0.9")
        private Double topP;
        
        @JsonProperty("maxOutputTokens")
        @Schema(description = "Max output tokens of the generation", example = "1024")
        private Integer maxOutputTokens;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Safety setting")
    public static class SafetySetting {
        @Schema(description = "Category of the safety setting", example = "HARM_CATEGORY_HATE_SPEECH")
        private String category;
        @Schema(description = "Threshold of the safety setting", example = "BLOCK_MEDIUM_AND_ABOVE")
        private String threshold;
    }

}
