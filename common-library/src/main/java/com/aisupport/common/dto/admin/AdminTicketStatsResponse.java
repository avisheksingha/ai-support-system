package com.aisupport.common.dto.admin;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTicketStatsResponse {
    private long ticketsToday;
    private long totalTickets;
    private long assignedTickets;
    private long activeTickets;
    private long resolvedToday;
    private Map<String, Long> departmentWorkload;
    private Map<String, Long> routingOverview;
}
