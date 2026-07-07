package com.aisupport.common.security;

import java.util.List;

/**
 * Shared, security-sensitive endpoint patterns reused across services.
 *
 * Exposed as an immutable List (not a String[]) so that "final" actually
 * means immutable here - a final array only locks the reference, not its
 * contents, and would let any caller silently rewrite these patterns at
 * runtime.
 */
public final class CommonSecurityEndpoints {

    public static final List<String> PUBLIC = List.of(
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/actuator/health",
            "/actuator/info"
    );

    private CommonSecurityEndpoints() {
    	// Prevent instantiation
    }
}