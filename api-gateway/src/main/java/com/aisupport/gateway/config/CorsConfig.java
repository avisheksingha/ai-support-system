package com.aisupport.gateway.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import com.aisupport.common.auth.SecurityConstants;

@Configuration
public class CorsConfig {

    @Bean
    CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Use allowedOriginPatterns instead of allowedOrigins when allowCredentials is true
        corsConfig.setAllowedOriginPatterns(List.of("*")); // NOSONAR: Wildcard cors is intended for this development stage.
        
        // Allow standard HTTP methods
        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Allow all headers (including Authorization and Content-Type)
        corsConfig.setAllowedHeaders(List.of("*"));
        
        // Crucial: If you are passing Authorization headers for JWTs, this must be true
        corsConfig.setAllowCredentials(true); 
        
        // Expose custom headers to the frontend so they can be read by JS
        corsConfig.setExposedHeaders(List.of(SecurityConstants.HEADER_ACCESS_TOKEN_REFRESH));
        
        // Cache the preflight response for 1 hour
        corsConfig.setMaxAge(3600L);

        // Apply this CORS configuration to all Gateway routes
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
