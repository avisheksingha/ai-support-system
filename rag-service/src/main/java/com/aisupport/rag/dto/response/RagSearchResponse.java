package com.aisupport.rag.dto.response;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RagSearchResponse {
    
    private String answer;
    private boolean knowledgeFound;
    
    // Extensibility fields for future enhancements
    private List<String> sources;
    private List<String> citations;
    private Double confidence;
    private Long executionTimeMs;
    private Map<String, Object> modelMetadata;
}
