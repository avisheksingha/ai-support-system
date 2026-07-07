package com.aisupport.analysis.config;

import java.util.List;
import java.util.stream.Stream;

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
	 * Service-specific public endpoints that are accessible without authentication.
	 */
	private static final List<String> SERVICE_SPECIFIC_PUBLIC = List.of(
	        "/api/v1/webhooks/provider/callback"
	);

	/**
	 * Combines common public endpoints with service-specific public endpoints.
	 */
	private static final String[] ALL_PUBLIC_ENDPOINTS = Stream.concat(
	        CommonSecurityEndpoints.PUBLIC.stream(),
	        SERVICE_SPECIFIC_PUBLIC.stream()
	).toArray(String[]::new);
    
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
	                    .requestMatchers(ALL_PUBLIC_ENDPOINTS).permitAll()
	                    .requestMatchers("/api/v1/analysis/**", "/internal/**").authenticated()
	                    .anyRequest().denyAll()
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
}
