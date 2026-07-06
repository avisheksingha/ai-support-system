package com.aisupport.rag.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "rag_responses",
    indexes = {
        @Index(name = "idx_rag_ticket_id", columnList = "ticket_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RagResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(columnDefinition = "TEXT")
    private String query;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String response;

    @Column(name = "model")
    private String model;
    
    @Builder.Default
    @Column(name = "knowledge_found", nullable = false)
    private Boolean knowledgeFound = Boolean.FALSE;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
