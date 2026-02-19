package com.aisupport.rule.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload for a routing rule")
public class RuleResponse {
    
    @Schema(description = "Unique identifier of the rule", example = "1")
    private Long id;

    @Schema(description = "Unique name of the rule", example = "High Urgency Billing Rule")
    private String ruleName;

    @Schema(description = "Human-readable description of the rule", example = "Routes critical billing issues to the billing team")
    private String description;

    @Schema(description = "Evaluation priority — higher value is evaluated first", example = "10")
    private Integer priority;

    @Schema(description = "Whether the rule is active", example = "true")
    private Boolean active;

    // Conditions
    @Schema(description = "Intent pattern to match", example = "BILLING|ACCOUNT")
    private String intentPattern;

    @Schema(description = "Sentiment pattern to match", example = "NEGATIVE|VERY_NEGATIVE")
    private String sentimentPattern;

    @Schema(description = "Urgency pattern to match", example = "HIGH|CRITICAL")
    private String urgencyPattern;

    @Schema(description = "Keyword patterns matched by the rule", example = "[\"billing\", \"invoice\"]")
    private List<String> keywordPatterns;

    // Actions
    @Schema(description = "Team assigned when rule matches", example = "billing-team")
    private String assignToTeam;

    @Schema(description = "Priority override applied when rule matches", example = "HIGH")
    private String priorityOverride;

    @Schema(description = "SLA hours override applied when rule matches", example = "4")
    private Integer slaHours;

    // Metadata
    @Schema(description = "Timestamp when the rule was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the rule was last updated")
    private LocalDateTime updatedAt;

    @Schema(description = "User who created the rule", example = "admin")
    private String createdBy;

    @Schema(description = "User who last updated the rule", example = "admin")
    private String updatedBy;
}
