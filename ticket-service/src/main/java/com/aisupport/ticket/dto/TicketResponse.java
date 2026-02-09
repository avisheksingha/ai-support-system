package com.aisupport.ticket.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {
    
    private Long id;
    private String ticketNumber;
    private String customerEmail;
    private String customerName;
    private String subject;
    private String message;
    private String status;
    private String priority;
    private String assignedTo;
    
    // Analysis results
    private String intent;
    private String sentiment;
    private String urgency;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
