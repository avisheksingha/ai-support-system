package com.aisupport.ticket.dto;

import java.time.LocalDateTime;

import com.aisupport.common.enums.TicketPriority;
import com.aisupport.common.enums.TicketStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload for a support ticket")
public class TicketResponse {
    
    @Schema(description = "Unique identifier for the ticket", example = "1")
    private Long id;
    
    @Schema(description = "Unique ticket number", example = "TICKET-2024-001")
    private String ticketNumber;
    
    @Schema(description = "Customer's email address", example = "user@example.com")
    private String customerEmail;
    
    @Schema(description = "Customer's full name", example = "Jane Doe")
    private String customerName;
    
    @Schema(description = "Ticket subject", example = "Cannot access my account")
    private String subject;
    
    @Schema(description = "Detailed message for the support ticket", example = "I am unable to log in with my credentials.")
    private String message;
    
    @Schema(description = "Current status of the ticket", example = "NEW")
    private TicketStatus status;
    
    @Schema(description = "Priority level of the ticket", example = "HIGH")
    private TicketPriority priority;
    
    @Schema(description = "Agent assigned to the ticket", example = "agent123")
    private String assignedTo;
    
    // Analysis results
    @Schema(description = "Detected intent of the ticket", example = "ACCOUNT_ACCESS")
    private String intent;
    
    @Schema(description = "Sentiment analysis result", example = "NEGATIVE")
    private String sentiment;
    
    @Schema(description = "Calculated urgency level", example = "HIGH")
    private String urgency;
    
    @Schema(description = "SLA hours applied to the ticket", example = "24")
    private Integer slaHours;
    
    @Schema(description = "AI suggested response for the ticket")
    private String ragResponse;

    @Schema(description = "When the RAG response was generated")
    private LocalDateTime ragGeneratedAt;
    
    @Schema(description = "Timestamp when the ticket was created")
    private LocalDateTime createdAt;
    
    @Schema(description = "Timestamp when the ticket was last updated")
    private LocalDateTime updatedAt;

}
