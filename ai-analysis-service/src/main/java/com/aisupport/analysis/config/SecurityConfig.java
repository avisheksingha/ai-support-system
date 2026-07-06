package com.aisupport.analysis.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.aisupport.common.exception.SecurityConfigurationException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.aisupport.common.security.CookieGuardFilter;
import com.aisupport.common.security.HeaderAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;



@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${security.cookie-guard.allowed-paths:}")
    private List<String> allowedPaths;

	
	/**
     * Endpoints that are publicly accessible without JWT authentication.
     */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/actuator/health",
            "/actuator/info",
            "/api/v1/analysis/public-statuses"
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
            .csrf(csrf -> {})
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                    .requestMatchers("/api/v1/analysis/**", "/internal/**").authenticated()
                    .anyRequest().denyAll()
            )

            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint((request, response, ex) ->
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
            )

            .addFilterBefore(headerAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(cookieGuardFilter, HeaderAuthenticationFilter.class);
            return http.build();
    } catch (Exception ex) {
            throw new SecurityConfigurationException("Failed to configure Spring Security filter chain", ex);
        }
    }

    @Bean
    CookieGuardFilter cookieGuardFilter() {
        return new CookieGuardFilter(allowedPaths);
    }
}
