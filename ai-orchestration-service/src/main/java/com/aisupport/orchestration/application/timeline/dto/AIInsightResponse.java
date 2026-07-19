package com.aisupport.orchestration.application.timeline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIInsightResponse {
    private String intent;
    private String sentiment;
    private String urgency;
    private Double confidenceScore;
    private String analysisProvider;
    private java.util.List<String> keywords;
    private String suggestedCategory;
}
