package com.aisupport.common.event;

import com.aisupport.common.enums.TicketPriority;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RoutingDecision(
    String assignToTeam,
    TicketPriority priority,
    Integer slaHours
) {}
