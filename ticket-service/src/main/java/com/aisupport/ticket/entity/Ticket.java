package com.aisupport.ticket.entity;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import com.aisupport.common.enums.TicketPriority;
import com.aisupport.common.enums.TicketStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "tickets",
    indexes = {
        @Index(name = "idx_ticket_status", columnList = "status"),
        @Index(name = "idx_ticket_assigned_to", columnList = "assigned_to"),
        @Index(name = "idx_ticket_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version // NEW: Optimistic locking
    private Long version;

    @Column(name = "ticket_number", unique = true, nullable = false, updatable = false)
    private String ticketNumber;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "customer_name")
    private String customerName;

    @Column(nullable = false, length = 500)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketPriority priority;

    @Column(name = "assigned_to")
    private String assignedTo;

    // AI Analysis fields
    @Column(name = "intent")
    private String intent;

    @Column(name = "sentiment")
    private String sentiment;

    @Column(name = "urgency")
    private String urgency;

    @Column(name = "sla_hours")
    private Integer slaHours;
    
    @Column(name = "rag_response", columnDefinition = "TEXT")
    private String ragResponse;

    @Column(name = "rag_generated_at")
    private LocalDateTime ragGeneratedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // NEW: State machine transitions
    private static final Map<TicketStatus, Set<TicketStatus>> VALID_TRANSITIONS = Map.of(
	    TicketStatus.NEW,          Set.of(TicketStatus.ANALYZING, TicketStatus.ASSIGNED), // fast-path
	    TicketStatus.ANALYZING,    Set.of(TicketStatus.ANALYZED),
	    TicketStatus.ANALYZED,     Set.of(TicketStatus.ASSIGNED),
	    TicketStatus.ASSIGNED,     Set.of(TicketStatus.IN_PROGRESS),
	    TicketStatus.IN_PROGRESS,  Set.of(TicketStatus.RESOLVED),
	    TicketStatus.RESOLVED,     Set.of(TicketStatus.CLOSED),
	    TicketStatus.CLOSED,       Set.of()
	);

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (status == null) {
            status = TicketStatus.NEW;
        }

        if (priority == null) {
            priority = TicketPriority.MEDIUM;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // NEW: Enforce state machine transitions
    public void transitionTo(TicketStatus newStatus) {
    	if (!VALID_TRANSITIONS.getOrDefault(this.status, Set.of()).contains(newStatus)) {
            throw new IllegalStateException(
                "Invalid transition from " + this.status + " to " + newStatus
            );
        }
        this.status = newStatus;
    }
}
