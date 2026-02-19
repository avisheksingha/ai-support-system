package com.aisupport.rule.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request payload for creating or updating a routing rule", requiredProperties = {"ruleName", "assignToTeam"})
public class RuleRequest {
    
    @Schema(description = "Unique name of the rule", example = "High Urgency Billing Rule")
    @NotBlank(message = "Rule name is required")
    @Size(max = 100, message = "Rule name must not exceed 100 characters")
    private String ruleName;

    @Schema(description = "Human-readable description of the rule", example = "Routes critical billing issues to the billing team")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Schema(description = "Evaluation priority — higher value is evaluated first", example = "10")
    @Min(value = 0, message = "Priority must be at least 0")
    private Integer priority;

    @Schema(description = "Whether the rule is active", example = "true")
    private Boolean active;

    // Conditions
    @Schema(description = "Intent pattern to match (use | for multiple values, * for wildcard)", example = "BILLING|ACCOUNT")
    private String intentPattern;

    @Schema(description = "Sentiment pattern to match", example = "NEGATIVE|VERY_NEGATIVE")
    private String sentimentPattern;

    @Schema(description = "Urgency pattern to match", example = "HIGH|CRITICAL")
    private String urgencyPattern;

    @Schema(description = "Keyword patterns to match (any match triggers the rule)", example = "[\"billing\", \"invoice\", \"charge\"]")
    private List<String> keywordPatterns;

    // Actions
    @Schema(description = "Team to assign the ticket to when rule matches", example = "billing-team")
    @NotBlank(message = "Team assignment is required")
    private String assignToTeam;

    @Schema(description = "Override ticket priority when rule matches", example = "HIGH")
    private String priorityOverride;

    @Schema(description = "SLA hours override when rule matches", example = "4")
    @Min(value = 1, message = "SLA hours must be at least 1")
    private Integer slaHours;
}
