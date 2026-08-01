package com.aisupport.ticket.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dashboard summary response from ticket service")
public class TicketDashboardSummaryResponse {
    @Schema(description = "Number of tickets assigned today")
    private long assignedToday;
    @Schema(description = "Total assigned tickets")
    private long totalAssigned;
    @Schema(description = "Critical priority ticket count")
    private long critical;
    @Schema(description = "High priority ticket count")
    private long high;
    @Schema(description = "Medium priority ticket count")
    private long medium;
    @Schema(description = "Low priority ticket count")
    private long low;
    
    @Schema(description = "Average wait time in minutes")
    private Long averageWaitTimeMins;
    @Schema(description = "Oldest ticket age in minutes")
    private Long oldestTicketAgeMins;
    
    @Schema(description = "Number of tickets nearing SLA breach")
    private long nearSlaBreach;
    @Schema(description = "Minutes until the next SLA breach")
    private Long nextSlaBreachMins;
    @Schema(description = "Average remaining SLA in minutes")
    private Long averageRemainingSlaMins;
    
    @Schema(description = "Number of tickets resolved today")
    private long resolvedToday;
    @Schema(description = "Average handle time in minutes")
    private Long averageHandleTimeMins;
    @Schema(description = "Average first response time in minutes")
    private Long averageFirstResponseTimeMins;
}
