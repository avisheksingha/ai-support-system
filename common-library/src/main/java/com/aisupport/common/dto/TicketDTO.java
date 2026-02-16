package com.aisupport.common.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Ticket DTO", requiredProperties = {"ticketNumber", "customerEmail", "subject", "message"})
public class TicketDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Schema(description = "ID of the ticket", example = "1")
	private Long id;
    
    @NotBlank(message = "Ticket number is required")
    @Schema(description = "Ticket number", example = "TICKET-123")
    private String ticketNumber;
    
    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Customer email", example = "[EMAIL_ADDRESS]")
    private String customerEmail;
    
    @Schema(description = "Customer name", example = "John Doe")
    private String customerName;
    
    @NotBlank(message = "Subject is required")
    @Size(max = 500, message = "Subject must not exceed 500 characters")
    @Schema(description = "Subject of the ticket", example = "Order Status")
    private String subject;
    
    @NotBlank(message = "Message is required")
    @Size(min = 10, message = "Message must be at least 10 characters")
    @Schema(description = "Message of the ticket", example = "I need help with my order.")
    private String message;
    
    @Schema(description = "Status of the ticket", example = "Open")
    private String status;
    @Schema(description = "Priority of the ticket", example = "High")
    private String priority;
    @Schema(description = "Assigned to of the ticket", example = "John Doe")
    private String assignedTo;

}
