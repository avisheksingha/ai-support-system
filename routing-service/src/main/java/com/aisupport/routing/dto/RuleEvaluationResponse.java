package com.aisupport.routing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleEvaluationResponse {
    
    private Long ticketId;
    private Long matchedRuleId;
    private String matchedRuleName;
    private String assignToTeam;
    private String priorityOverride;
    private Integer slaHours;
    private String reason;
    private Long evaluationTimeMs;
}