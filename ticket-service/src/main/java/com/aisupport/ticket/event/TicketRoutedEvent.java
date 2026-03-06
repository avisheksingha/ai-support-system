package com.aisupport.ticket.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketRoutedEvent {

    private Long ticketId;
    private String assignToTeam;
    private String priority;
    private Integer slaHours;
}
