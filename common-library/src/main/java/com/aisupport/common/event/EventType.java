package com.aisupport.common.event;

public enum EventType {

    // Ticket Lifecycle Events
    TICKET_CREATED,
    TICKET_UPDATED,
    CUSTOMER_REPLY_ADDED,
    AGENT_REPLY_ADDED,

    // AI & Orchestration Events
    TICKET_ANALYZED,
    TICKET_ROUTED,
    TICKET_RAG_RESPONSE_GENERATED,
    TICKET_ORCHESTRATED
}
