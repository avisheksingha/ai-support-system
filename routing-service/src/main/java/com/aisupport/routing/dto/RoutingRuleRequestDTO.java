package com.aisupport.routing.dto;

import java.util.List;

import com.aisupport.common.enums.TicketPriority;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingRuleRequestDTO {

    @NotBlank(message = "Rule name is required")
    @Size(max = 100, message = "Rule name cannot exceed 100 characters")
    private String ruleName;

    private String description;

    @NotNull(message = "Priority is required")
    @Min(value = 0, message = "Priority must be 0 or greater")
    @Builder.Default
    private Integer priority = 0;

    @Builder.Default
    private Boolean active = true;

    // Condition fields
    @Size(max = 100, message = "Intent pattern cannot exceed 100 characters")
    private String intentPattern;

    @Size(max = 50, message = "Sentiment pattern cannot exceed 50 characters")
    private String sentimentPattern;

    @Size(max = 50, message = "Urgency pattern cannot exceed 50 characters")
    private String urgencyPattern;

    private List<String> keywordPatterns;

    // Action fields
    @NotBlank(message = "Target team is required")
    @Size(max = 100, message = "Target team cannot exceed 100 characters")
    private String assignToTeam;

    private TicketPriority priorityOverride;

    @Min(value = 1, message = "SLA hours must be at least 1")
    private Integer slaHours;
}
