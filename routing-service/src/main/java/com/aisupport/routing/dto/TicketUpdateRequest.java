package com.aisupport.routing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketUpdateRequest {
    
    private String status;
    private String priority;
    private String assignedTo;
}