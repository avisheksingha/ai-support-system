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
import com.aisupport.common.security.CookieGuardFilter;
import com.aisupport.common.security.HeaderAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;



@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
	
	/**
     * Endpoints that are publicly accessible without JWT authentication.
     */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/actuator/health",
            "/actuator/info"
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
	 * Configures the security filter chain for the application.
	 *
	 * @param http the HttpSecurity object to configure
	 * @param headerAuthFilter the custom header authentication filter
	 * @return the configured SecurityFilterChain
	 * @throws Exception if an error occurs during configuration
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
            .csrf(csrf -> csrf.disable()) // NOSONAR
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                    .requestMatchers(
                            "/api/v1/auth/register",
                            "/api/v1/auth/login",
                            "/api/v1/auth/refresh"
                    ).permitAll()
                    .requestMatchers("/api/v1/auth/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            )

            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint((request, response, ex) ->
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
            )

            .addFilterBefore(cookieGuardFilter, HeaderAuthenticationFilter.class)
            .addFilterBefore(headerAuthFilter, UsernamePasswordAuthenticationFilter.class);

            return http.build();
        } catch (Exception ex) {
            throw new SecurityConfigurationException("Failed to configure Spring Security filter chain", ex);
        }
    }

    @Bean
    CookieGuardFilter cookieGuardFilter(
            @Value("${security.cookie-guard.allowed-paths:}") List<String> allowedPaths) {
        return new CookieGuardFilter(allowedPaths);
    }
}
