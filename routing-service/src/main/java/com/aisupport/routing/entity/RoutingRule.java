package com.aisupport.routing.entity;

import com.aisupport.common.enums.TicketPriority;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "routing_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutingRule extends AuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "rule_name", unique = true, nullable = false, length = 100)
    private String ruleName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 0;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
    
    // Condition fields
    @Column(name = "intent_pattern", length = 100)
    private String intentPattern;
    
    @Column(name = "sentiment_pattern", length = 50)
    private String sentimentPattern;
    
    @Column(name = "urgency_pattern", length = 50)
    private String urgencyPattern;
    
    @Column(name = "keyword_patterns", columnDefinition = "TEXT[]")
    private String[] keywordPatterns;
    
    // Action fields
    @Column(name = "assign_to_team", nullable = false, length = 100)
    private String assignToTeam;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority_override")
    private TicketPriority priorityOverride;
    
    @Column(name = "sla_hours")
    private Integer slaHours;
    
    // Metadata    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
