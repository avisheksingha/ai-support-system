package com.aisupport.rule.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleResponse {
    
    private Long id;
    private String ruleName;
    private String description;
    private Integer priority;
    private Boolean active;
    
    // Conditions
    private String intentPattern;
    private String sentimentPattern;
    private String urgencyPattern;
    private List<String> keywordPatterns;
    
    // Actions
    private String assignToTeam;
    private String priorityOverride;
    private Integer slaHours;
    
    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
