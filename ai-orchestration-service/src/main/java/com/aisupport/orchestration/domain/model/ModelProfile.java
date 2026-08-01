package com.aisupport.orchestration.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ModelProfile {
    private final String id;
    private final String provider;
    private final String name;
    
    private final int maxContextTokens;
    private final int maxOutputTokens;
    private final double defaultTemperature;
    
    private final boolean supportsToolCalling;
    private final boolean supportsStreaming;
    private final boolean supportsVision;
    private final boolean supportsStructuredOutput;
}
