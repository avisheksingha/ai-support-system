package com.aisupport.rule.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleEvaluationRequest {
    
    @NotNull(message = "Ticket ID is required")
    private Long ticketId;
    
    @NotNull(message = "Intent is required")
    private String intent;
    
    @NotNull(message = "Sentiment is required")
    private String sentiment;
    
    @NotNull(message = "Urgency is required")
    private String urgency;
    
    private List<String> keywords;
}
