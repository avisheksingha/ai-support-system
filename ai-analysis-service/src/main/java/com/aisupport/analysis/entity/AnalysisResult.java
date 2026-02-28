package com.aisupport.analysis.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "analysis_results",
    indexes = {
        @Index(name = "idx_analysis_ticket_id", columnList = "ticket_id"),
        @Index(name = "idx_analysis_intent", columnList = "intent"),
        @Index(name = "idx_analysis_urgency", columnList = "urgency"),
        @Index(name = "idx_analysis_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version // NEW: Optimistic locking
    private Long version;

    @Column(name = "ticket_id", unique = true, nullable = false, updatable = false) // NEW: Unique constraint to ensure one analysis per ticket
    private Long ticketId;

    @Column(nullable = false, length = 100)
    private String intent;

    @Column(nullable = false, length = 50)
    private String sentiment;

    @Column(nullable = false, length = 50)
    private String urgency;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(columnDefinition = "TEXT[]")
    private String[] keywords;

    @Column(name = "suggested_category")
    private String suggestedCategory;

    @Column(name = "raw_response", columnDefinition = "TEXT")
    private String rawResponse;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
