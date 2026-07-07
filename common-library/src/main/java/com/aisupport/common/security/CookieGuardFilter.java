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
 *
 * This enforces the invariant that the API is strictly stateless
 * (Authorization headers only) and prevents accidental introduction
 * of cookie-based CSRF vulnerabilities.
 *
 * Implemented as a servlet Filter (not an MVC HandlerInterceptor) so it
 * executes before Spring Security's authentication filters run - see
 * ADR-004 addendum. Register with:
 *
 *   .addFilterBefore(cookieGuardFilter, HeaderAuthenticationFilter.class)
 */
public class CookieGuardFilter extends OncePerRequestFilter {

    private final List<String> allowedPaths;

    public CookieGuardFilter(List<String> allowedPaths) {
        this.allowedPaths = allowedPaths != null ? allowedPaths : List.of();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getHeader("Cookie") != null && !isAllowed(request.getRequestURI())) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cookie auth is not supported by this service");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isAllowed(String uri) {
        return uri != null && allowedPaths.stream().anyMatch(uri::startsWith);
    }
}