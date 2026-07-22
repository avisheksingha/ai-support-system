package com.aisupport.common.event;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalysisResult(
    String intent,
    String sentiment,
    String urgency,
    Double confidenceScore,
    List<String> keywords,
    String suggestedCategory
) {}
