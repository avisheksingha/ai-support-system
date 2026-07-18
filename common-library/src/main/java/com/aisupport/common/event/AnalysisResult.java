package com.aisupport.common.event;

import java.util.List;

public record AnalysisResult(
    String intent,
    String sentiment,
    String urgency,
    Double confidenceScore,
    List<String> keywords,
    String suggestedCategory
) {}
