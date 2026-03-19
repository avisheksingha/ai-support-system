package com.aisupport.common.constant;

public final class KafkaTopics {
    private KafkaTopics() {} // prevent instantiation

    public static final String TICKET_CREATED  = "ticket-created";
    public static final String TICKET_ANALYZED = "ticket-analyzed";
    public static final String TICKET_ROUTED   = "ticket-routed";
    public static final String TICKET_RAG_RESPONSE = "ticket-rag-response";
}
