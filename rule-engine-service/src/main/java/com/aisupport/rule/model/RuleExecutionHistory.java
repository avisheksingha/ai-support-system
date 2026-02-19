package com.aisupport.rule.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rule_execution_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleExecutionHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "rule_id", nullable = false)
    private Long ruleId;
    
    @Column(name = "ticket_id")
    private Long ticketId;
    
    @Column(nullable = false)
    private Boolean matched;
    
    @Column(name = "execution_time_ms", nullable = false)
    private Long executionTimeMs;
    
    @Column(name = "executed_at", nullable = false, updatable = false)
    private LocalDateTime executedAt;
    
    @PrePersist
    protected void onCreate() {
        executedAt = LocalDateTime.now();
    }
}
