package com.aisupport.auth.entity;

import com.aisupport.common.enums.UserRole;

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

/**
 * Represents an authenticated user of the AI Support System.
 *
 * Notes:
 * - Uses Hibernate's automatic auditing for timestamps.
 * - createdAt is populated once when the entity is first persisted.
 * - updatedAt is automatically updated whenever the entity changes.
 * - Stores timestamps as Instant (UTC) for consistency across microservices.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends AuditableEntity {
    
    /**
     * Primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique email used for login.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * BCrypt/Argon2 hashed password.
     * Never store plain text passwords.
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * Display name of the user.
     */
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    /**
     * User role.
     * Examples: ADMIN, AGENT, CUSTOMER.
     *
     * Recommendation:
     * Replace String with an enum as the project evolves.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role; // ADMIN, AGENT, CUSTOMER

    /**
     * Whether the account is active.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

     /**
     * Whether the account has been locked.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean locked = false;
}
