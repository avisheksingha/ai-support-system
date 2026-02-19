package com.aisupport.rule.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload to evaluate routing rules against a ticket's AI analysis results",
        requiredProperties = {"ticketId", "intent", "sentiment", "urgency"})
public class RuleEvaluationRequest {

    @NotNull(message = "Ticket ID is required")
    @Schema(description = "ID of the ticket to evaluate rules for", example = "42")
    private Long ticketId;

    @NotBlank(message = "Intent is required")
    @Schema(description = "Detected intent from AI analysis", example = "BILLING")
    private String intent;

    @NotBlank(message = "Sentiment is required")
    @Schema(description = "Detected sentiment from AI analysis", example = "NEGATIVE")
    private String sentiment;

    @NotBlank(message = "Urgency is required")
    @Schema(description = "Detected urgency from AI analysis", example = "HIGH")
    private String urgency;

    @Schema(description = "Keywords extracted by AI analysis", example = "[\"billing\", \"invoice\"]")
    private List<String> keywords;
}
