package com.aisupport.common.enums;

/**
 * Supported user roles within the system.
 */
public enum UserRole {

    /**
     * Full system access.
     */
    ADMIN,

    /**
     * Handles support tickets.
     */
    AGENT,

    /**
     * End user who creates support tickets.
     */
    CUSTOMER
}
