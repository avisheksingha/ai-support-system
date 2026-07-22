package com.aisupport.orchestration.application.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketSummaryDTO {
    private String ticketNumber;
    private String subject;
    private String status;
    private String priority;
    private String assignedSupportStatus; // e.g. "Assigned", "Pending Assignment"
    private String estimatedResponse;     // e.g. "Within 4 hours", "Standard"
    private String lastUpdated;           // e.g. "just now", "2 hours ago", or raw ISO string if we format in frontend
}
