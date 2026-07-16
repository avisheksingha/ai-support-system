package com.aisupport.orchestration.application.timeline.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIInsightResponse {
    private Long ticketId;
    private String intent;
    private String sentiment;
    private String urgency;
    private Double confidenceScore;
    private List<String> keywords;
    private String suggestedCategory;
    private String analyzedAt;
    private String analysisProvider;
}
