package com.aisupport.common.event;

import com.aisupport.common.enums.TicketPriority;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketRoutedEvent {

    private Long ticketId;
    private String assignToTeam;
    private TicketPriority priority;
    private Integer slaHours;
    private String intent;
    private String sentiment;
    private String urgency;
}
