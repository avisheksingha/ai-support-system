package com.aisupport.rule.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload representing a single rule execution history entry")
public class RuleExecutionHistoryResponse {

    @Schema(description = "Unique identifier of the history record", example = "101")
    private Long id;

    @Schema(description = "ID of the rule that was evaluated", example = "3")
    private Long ruleId;

    @Schema(description = "ID of the ticket the rule was evaluated against", example = "42")
    private Long ticketId;

    @Schema(description = "Whether the rule matched the ticket", example = "true")
    private Boolean matched;

    @Schema(description = "Time taken to execute this rule in milliseconds", example = "5")
    private Long executionTimeMs;

    @Schema(description = "Timestamp when this rule was executed")
    private LocalDateTime executedAt;
}
