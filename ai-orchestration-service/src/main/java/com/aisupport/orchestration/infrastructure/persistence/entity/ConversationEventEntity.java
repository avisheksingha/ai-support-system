package com.aisupport.orchestration.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "conversation_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationEventEntity {
    
    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    
    private String conversationId;
    private String author;
    private String type; // USER_MESSAGE, AGENT_MESSAGE, AI_REPLY, SYSTEM_EVENT
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    private Instant createdAt;
}
