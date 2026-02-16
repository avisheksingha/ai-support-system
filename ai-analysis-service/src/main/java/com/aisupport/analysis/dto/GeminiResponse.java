package com.aisupport.analysis.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Response payload from Gemini AI")
public class GeminiResponse {

    @Schema(description = "List of candidates")
    private List<Candidate> candidates;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "Candidate for the response")
    public static class Candidate {
        @Schema(description = "Content of the response", example = "Hello, I need help with my account.")
        private Content content;
        @Schema(description = "Finish reason of the response", example = "STOP")
        private String finishReason;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "Content of the response")
    public static class Content {
        @Schema(description = "List of parts", example = "Hello, I need help with my account.")
        private List<Part> parts;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "Part of the response")
    public static class Part {
        @Schema(description = "Text of the part", example = "Hello, I need help with my account.")
        private String text;
    }
}
