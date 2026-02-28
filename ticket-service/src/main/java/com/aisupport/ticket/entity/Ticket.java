package com.aisupport.ticket.model;

import java.time.LocalDateTime;

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
import jakarta.validation.constraints.Email;
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
    @Email(message = "Invalid email format")
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
    private Priority priority;

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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (status == null) {
            status = TicketStatus.NEW;
        }

        if (priority == null) {
            priority = Priority.MEDIUM;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // NEW: Enforce state machine transitions
    public void transitionTo(TicketStatus newStatus) {

        if (this.status == TicketStatus.CLOSED) {
            throw new IllegalStateException("Closed tickets cannot transition.");
        }

        // Example transition validation
        if (this.status == TicketStatus.NEW && newStatus == TicketStatus.ANALYZED) {
            throw new IllegalStateException("Must pass through ANALYZING first.");
        }

        this.status = newStatus;
    }

    public enum TicketStatus {
        NEW,
        ANALYZING,
        ANALYZED,
        ASSIGNED,
        IN_PROGRESS,
        RESOLVED,
        CLOSED
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
