package com.aisupport.common.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Routing decision DTO")
public class RoutingDecisionDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Schema(description = "ID of the ticket", example = "1")
	private Long ticketId;
	@Schema(description = "Team to assign the ticket to", example = "Sales")
    private String assignToTeam;
	@Schema(description = "Priority of the ticket", example = "High")
    private String priority;
	@Schema(description = "Name of the rule that was applied", example = "High Priority Rule")
    private String ruleName;
	@Schema(description = "Reason for the routing decision", example = "High priority ticket")
    private String reason;
}
