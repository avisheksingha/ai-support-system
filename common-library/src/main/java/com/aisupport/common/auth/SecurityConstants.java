package com.aisupport.common.auth;

public class SecurityConstants {
	
    private SecurityConstants() {
        /* This utility class should not be instantiated */
    }

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    
    // Custom Headers passed by API Gateway to downstream services
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLE = "X-User-Role";
    public static final String HEADER_USER_EMAIL = "X-User-Email";
    
    // Role Prefixes
    public static final String ROLE_PREFIX = "ROLE_";
}
