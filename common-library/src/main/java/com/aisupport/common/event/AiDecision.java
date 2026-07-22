package com.aisupport.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiDecision(
    String aiSummary,
    String suggestedReply,
    Double confidence,
    String decisionReason
) {
    public AiDecision(String aiSummary, String suggestedReply, Double confidence) {
        this(aiSummary, suggestedReply, confidence, null);
    }
}
