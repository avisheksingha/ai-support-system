package com.aisupport.common.security;

import java.io.IOException;
import java.util.List;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Runtime guard that rejects any request containing a Cookie header
 * unless the path is explicitly whitelisted.
 * This filter runs at the very beginning of the Spring Security chain
 * to enforce the invariant that the API is strictly stateless
 * and prevents accidental introduction of cookie-based CSRF vulnerabilities.
 */
public class CookieGuardFilter extends OncePerRequestFilter {

    private final List<String> allowedPaths;

    public CookieGuardFilter(List<String> allowedPaths) {
        this.allowedPaths = allowedPaths != null ? allowedPaths : List.of();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        if (request.getHeader("Cookie") != null && !isAllowed(request.getRequestURI())) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cookie auth is not supported by this service");
            return;
        }
        
        chain.doFilter(request, response);
    }

    private boolean isAllowed(String uri) {
        return uri != null && allowedPaths.stream().anyMatch(uri::startsWith);
    }
}
