package com.aisupport.orchestration.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDTO {
    private Long id;
    private String ticketNumber;
    private Long customerUserId;
    private String customerEmail;
    private String customerName;
    private String subject;
    private String message;
    private String status;
    private String priority;
    private String assignedTo;
    private String ragResponse;
    private String ragGeneratedAt;
    private Integer slaHours;
    private String customerTier;
    private String channel;
    private String createdAt;
    private String updatedAt;
}
