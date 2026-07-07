package com.aisupport.common.security;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Locks in the ADR-004 invariant: any cookie-bearing request is rejected
 * unless the path is explicitly whitelisted - and rejection happens
 * without ever reaching downstream filters (i.e. before authentication).
 */
class CookieGuardFilterTest {

    @Test
    void rejectsCookieOnNonWhitelistedPath() throws Exception {
        CookieGuardFilter filter = new CookieGuardFilter(List.of("/api/v1/oauth2/callback"));
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("Cookie")).thenReturn("JSESSIONID=abc123");
        when(request.getRequestURI()).thenReturn("/api/v1/tickets");

        filter.doFilterInternal(request, response, chain);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
        verifyNoInteractions(chain);
    }

    @Test
    void allowsRequestWithNoCookieHeader() throws Exception {
        CookieGuardFilter filter = new CookieGuardFilter(List.of());
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("Cookie")).thenReturn(null);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(response);
    }

    @Test
    void allowsCookieOnWhitelistedPath() throws Exception {
        CookieGuardFilter filter = new CookieGuardFilter(List.of("/api/v1/oauth2/callback"));
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("Cookie")).thenReturn("JSESSIONID=abc123");
        when(request.getRequestURI()).thenReturn("/api/v1/oauth2/callback");

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
