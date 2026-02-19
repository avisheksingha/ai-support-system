package com.aisupport.rule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response from rule evaluation — the first matching rule's routing decision")
public class RuleEvaluationResponse {

    @Schema(description = "ID of the ticket that was evaluated", example = "42")
    private Long ticketId;

    @Schema(description = "ID of the matched rule (null if no rule matched)", example = "3")
    private Long matchedRuleId;

    @Schema(description = "Name of the matched rule", example = "High Urgency Billing Rule")
    private String matchedRuleName;

    @Schema(description = "Team the ticket was assigned to", example = "billing-team")
    private String assignToTeam;

    @Schema(description = "Priority override applied to the ticket", example = "HIGH")
    private String priorityOverride;

    @Schema(description = "SLA hours applied to the ticket", example = "4")
    private Integer slaHours;

    @Schema(description = "Human-readable reason for the routing decision", example = "Matched rule: High Urgency Billing Rule, Intent: BILLING, Urgency: HIGH")
    private String reason;

    @Schema(description = "Total evaluation time in milliseconds", example = "12")
    private Long evaluationTimeMs;
}
