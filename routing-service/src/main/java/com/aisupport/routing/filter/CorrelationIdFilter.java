package com.aisupport.routing.filter;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import com.aisupport.common.constant.HttpHeaders;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String correlationId = request.getHeader(HttpHeaders.CORRELATION_ID);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString(); // fallback if called directly
        }

        MDC.put(MDC_KEY, correlationId);

        // propagate to response so caller can trace it back
        response.setHeader(HttpHeaders.CORRELATION_ID, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY); // always clean up — prevents thread pool leaks
        }
    }
}
