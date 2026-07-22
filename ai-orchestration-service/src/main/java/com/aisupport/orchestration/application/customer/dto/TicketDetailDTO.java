package com.aisupport.orchestration.application.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDetailDTO {
    private String ticketNumber;
    private String subject;
    private String message;
    private String status;
    private String priority;
    private String assignedSupportStatus;
    private String estimatedResponse;
    private String createdAt;
    private String lastUpdated;
}
