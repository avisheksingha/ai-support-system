package com.aisupport.common.event;

import com.aisupport.common.enums.TicketStatus;

public record TicketOrchestratedEvent(
    Long ticketId,
    TicketStatus ticketStatus,
    AnalysisResult analysis,
    RoutingDecision routing,
    KnowledgeContext knowledge,
    AiDecision aiDecision,
    EventMetadata metadata
) {}
