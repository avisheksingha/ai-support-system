package com.aisupport.common.event;

public record AnalysisResult(
    String intent,
    String sentiment,
    String urgency
) {}
