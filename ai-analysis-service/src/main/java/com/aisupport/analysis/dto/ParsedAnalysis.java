package com.aisupport.analysis.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Parsed analysis of the support ticket")
public class ParsedAnalysis {
    
    @Schema(description = "Detected intent of the ticket", example = "ACCOUNT_ACCESS")
    private String intent;
    
    @Schema(description = "Sentiment analysis result", example = "NEGATIVE")
    private String sentiment;
    
    @Schema(description = "Calculated urgency level", example = "HIGH")
    private String urgency;
    
    @JsonProperty("confidence_score")
    @Schema(description = "Confidence score of the analysis", example = "0.95")
    private Double confidenceScore;
    
    @Schema(description = "List of keywords extracted from the ticket", example = "["login", "password", "error"]")
    private List<String> keywords;
    
    @JsonProperty("suggested_category")
    @Schema(description = "Suggested category for the ticket", example = "ACCOUNT_ACCESS")
    private String suggestedCategory;

}
