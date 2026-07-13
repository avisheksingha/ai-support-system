package com.aisupport.common.event;

import com.aisupport.common.enums.TicketPriority;

public record RoutingDecision(
    String assignToTeam,
    TicketPriority priority,
    Integer slaHours
) {}
