package com.aisupport.routing.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleEvaluationRequest {
    
    private Long ticketId;
    private String intent;
    private String sentiment;
    private String urgency;
    private List<String> keywords;
}