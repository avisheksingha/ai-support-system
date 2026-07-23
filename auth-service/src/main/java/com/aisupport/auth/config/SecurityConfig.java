package com.aisupport.auth.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.aisupport.common.exception.SecurityConfigurationException;
import com.aisupport.common.security.CommonSecurityEndpoints;
import com.aisupport.common.security.CookieGuardFilter;
import com.aisupport.common.security.HeaderAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    /**
	 * Endpoints related to authentication that are publicly accessible.
	 */
    private static final String[] PUBLIC_AUTH_ENDPOINTS = {
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/internal/**"
    };

    /**
     * Creates a bean for the custom header authentication filter.
     *
     * @return a new instance of HeaderAuthenticationFilter
     */
    @Bean
    HeaderAuthenticationFilter headerAuthenticationFilter() {
        return new HeaderAuthenticationFilter();
    }

    /**
     * Runtime guard enforcing ADR-004: rejects any request carrying a Cookie
     * header unless the path is explicitly whitelisted via configuration.
     *
     * @param allowedPaths externally configured cookie-allowed path prefixes
     * @return a new instance of CookieGuardFilter
     */
    @Bean
    CookieGuardFilter cookieGuardFilter(
            @Value("${security.cookie-guard.allowed-paths:}") List<String> allowedPaths) {
        return new CookieGuardFilter(allowedPaths);
    }

    /**
     * Configures the security filter chain for the application.
     *
     * @param http the HttpSecurity object to configure
     * @param headerAuthFilter the custom header authentication filter
     * @param cookieGuardFilter the runtime stateless-invariant guard (ADR-004)
     * @return the configured SecurityFilterChain
     */
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            HeaderAuthenticationFilter headerAuthFilter,
            CookieGuardFilter cookieGuardFilter
    ) {
        try {
            http
                // codeql[java/spring-disabled-csrf-protection] - stateless REST API, enforced via CookieGuardFilter; see ADR-004
                .csrf(csrf -> csrf.disable()) // NOSONAR: stateless JWT API, no session/cookie auth - see ADR-004
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(CommonSecurityEndpoints.PUBLIC.toArray(String[]::new)).permitAll()
                        .requestMatchers(PUBLIC_AUTH_ENDPOINTS).permitAll()
                        .requestMatchers("/api/v1/auth/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, ex) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                )
                
                // Order matters here: HeaderAuthenticationFilter is a custom filter
                // with no preset order, so it must be registered first (anchored to
                // the well-known UsernamePasswordAuthenticationFilter) before we can
                // anchor CookieGuardFilter to it. Final runtime order is still
                // CookieGuardFilter -> HeaderAuthenticationFilter -> UsernamePasswordAuthenticationFilter.
                
                // See ADR-004 addendum.
                .addFilterBefore(headerAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(cookieGuardFilter, HeaderAuthenticationFilter.class);

            return http.build();
        } catch (Exception ex) {
            throw new SecurityConfigurationException("Failed to configure Spring Security filter chain", ex);
        }
    }
}