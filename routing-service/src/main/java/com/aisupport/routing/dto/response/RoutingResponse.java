package com.aisupport.routing.dto.response;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Result of the routing rule execution for a ticket")
public class RoutingResponse {

    @Schema(description = "Primary key ID", example = "1")
    private Long id;

    @Schema(description = "Ticket ID", example = "1001")
    private Long ticketId;

    @Schema(description = "Team or department the ticket was routed to", example = "L2 Technical Support")
    private String department;

    @Schema(description = "Agent assigned, if any")
    private String assignedAgent;

    @Schema(description = "Confidence score of the routing rule", example = "0.95")
    private Double confidenceScore;

    @Schema(description = "Reasoning or explanation from the rule description", example = "Ticket intent matched technical issue with high urgency.")
    private String reason;
    
    @Schema(description = "Name of the rule triggered", example = "TECHNICAL_URGENT_RULE")
    private String ruleName;
    
    @Schema(description = "Version of the rule triggered", example = "3")
    private Integer ruleVersion;

    @Schema(description = "Timestamp when the routing occurred")
    private Instant executedAt;
}
