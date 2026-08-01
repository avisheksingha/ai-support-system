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
public class AiDecisionContextDTO {
    private String intent;
    private String category;
    private Double confidence;
    private Integer retrievedArticleCount;
    private List<String> matchedArticles;
    private String routingDecision;
    private String decisionReason;
    private String routingExplanation;
    private boolean retrievalFallbackUsed;
}
