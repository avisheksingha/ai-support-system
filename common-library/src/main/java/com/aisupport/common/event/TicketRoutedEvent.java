package com.aisupport.common.event;

import com.aisupport.common.enums.TicketPriority;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketRoutedEvent {

    private Long ticketId;
    private String assignToTeam;
    private TicketPriority priority;
    private Integer slaHours;
    private String intent;
    private String sentiment;
    private String urgency;
}
