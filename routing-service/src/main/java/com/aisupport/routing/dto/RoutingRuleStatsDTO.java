package com.aisupport.routing.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingRuleStatsDTO {
    private long totalRules;
    private long activeRules;
    private long inactiveRules;
    private long totalTeamsCount;
    private List<String> targetTeams;
}
