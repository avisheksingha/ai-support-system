package com.aisupport.rule.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleRequest {
    
    @NotBlank(message = "Rule name is required")
    @Size(max = 100, message = "Rule name must not exceed 100 characters")
    private String ruleName;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @Min(value = 0, message = "Priority must be at least 0")
    private Integer priority;
    
    private Boolean active;
    
    // Conditions
    private String intentPattern;
    private String sentimentPattern;
    private String urgencyPattern;
    private List<String> keywordPatterns;
    
    // Actions
    @NotBlank(message = "Team assignment is required")
    private String assignToTeam;
    
    private String priorityOverride;
    
    @Min(value = 1, message = "SLA hours must be at least 1")
    private Integer slaHours;
}
