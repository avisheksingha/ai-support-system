package com.aisupport.common.event;

public record AiDecision(
    String aiSummary,
    String suggestedReply,
    Double confidence
) {}
