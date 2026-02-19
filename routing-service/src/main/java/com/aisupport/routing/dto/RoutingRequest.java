package com.aisupport.routing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutingRequest {
    
    @NotNull(message = "Ticket ID is required")
    private Long ticketId;
}