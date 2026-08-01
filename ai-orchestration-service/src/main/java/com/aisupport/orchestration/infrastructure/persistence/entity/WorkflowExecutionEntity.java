package com.aisupport.orchestration.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.aisupport.orchestration.domain.state.WorkflowState;
import com.aisupport.orchestration.domain.workflow.TicketContextSnapshot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "workflow_executions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowExecutionEntity {
    @Id
    private String id;
    private String definitionId;
    private Integer version;
    private String correlationId;
    private Long ticketId;
    private String conversationId;
    
    @Enumerated(EnumType.STRING)
    private WorkflowState state;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> attributes;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ticket_context", columnDefinition = "jsonb")
    private TicketContextSnapshot ticketContext;
    
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;
    
    private String currentStep;
    
    @Builder.Default
    private int recoveryCount = 0;
}
