package com.aisupport.routing.dto;

import java.time.Instant;
import java.util.List;

import com.aisupport.common.enums.TicketPriority;
import com.aisupport.routing.entity.RoutingRule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingRuleDTO {
    private Long id;
    private Integer ruleVersion;
    private String ruleName;
    private String description;
    private Integer priority;
    private Boolean active;
    
    // Condition fields
    private String intentPattern;
    private String sentimentPattern;
    private String urgencyPattern;
    private List<String> keywordPatterns;
    
    // Action fields
    private String assignToTeam;
    private TicketPriority priorityOverride;
    private Integer slaHours;
    
    // Metadata
    private String createdBy;
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;

    public static RoutingRuleDTO fromEntity(RoutingRule entity) {
        if (entity == null) return null;
        return RoutingRuleDTO.builder()
                .id(entity.getId())
                .ruleVersion(entity.getRuleVersion())
                .ruleName(entity.getRuleName())
                .description(entity.getDescription())
                .priority(entity.getPriority())
                .active(entity.getActive())
                .intentPattern(entity.getIntentPattern())
                .sentimentPattern(entity.getSentimentPattern())
                .urgencyPattern(entity.getUrgencyPattern())
                .keywordPatterns(entity.getKeywordPatterns() != null ? List.of(entity.getKeywordPatterns()) : List.of())
                .assignToTeam(entity.getAssignToTeam())
                .priorityOverride(entity.getPriorityOverride())
                .slaHours(entity.getSlaHours())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
