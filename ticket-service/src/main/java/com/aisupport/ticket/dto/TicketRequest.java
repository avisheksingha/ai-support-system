package com.aisupport.ticket.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for creating a support ticket")
public class TicketRequest {
    
    @Schema(description = "Customer's email address", example = "user@example.com", required = true)
    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;
    
    @Schema(description = "Customer's full name", example = "Jane Doe")
    private String customerName;
    
    @Schema(description = "Ticket subject", example = "Cannot access my account", required = true)
    @NotBlank(message = "Subject is required")
    @Size(max = 500, message = "Subject must not exceed 500 characters")
    private String subject;
    
    @Schema(description = "Detailed message for the support ticket", example = "I am unable to log in with my credentials.", required = true)
    @NotBlank(message = "Message is required")
    @Size(min = 10, message = "Message must be at least 10 characters")
    private String message;

}
