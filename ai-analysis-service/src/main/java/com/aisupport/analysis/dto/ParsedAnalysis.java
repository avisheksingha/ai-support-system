package com.aisupport.analysis.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParsedAnalysis {
    
    private String intent;
    private String sentiment;
    private String urgency;
    
    @JsonProperty("confidence_score")
    private Double confidenceScore;
    
    private List<String> keywords;
    
    @JsonProperty("suggested_category")
    private String suggestedCategory;

}
