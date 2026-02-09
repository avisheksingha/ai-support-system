package com.aisupport.common.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutingDecisionDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Long ticketId;
    private String assignToTeam;
    private String priority;
    private String ruleName;
    private String reason;
}
