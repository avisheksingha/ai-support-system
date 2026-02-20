package com.aisupport.ticket.config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Configuration
public class WebClientConfig {
	
	private static final int CONNECT_TIMEOUT = 10000; // 10 seconds
    private static final int READ_TIMEOUT = 60000; // 1 minute
    private static final int WRITE_TIMEOUT = 60000; // 1 minute
    private static final int MAX_IN_MEMORY_SIZE = 10 * 1024 * 1024; // 10MB
    
    private static final boolean LOGGING_ENABLED = true; // Could be made configurable
    private static final boolean LOG_REQUESTS = true;
    private static final boolean LOG_RESPONSES = true;

    @Bean
    @LoadBalanced
    WebClient.Builder loadBalancedWebClientBuilder() {
    	
    	// Configure HttpClient with timeouts and connection pool settings
    	HttpClient httpClient = HttpClient.create()
    			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT) // connect timeout
    			.responseTimeout(Duration.ofMillis(READ_TIMEOUT)) // read timeout
    			.doOnConnected(conn ->
    				conn.addHandlerLast(new ReadTimeoutHandler(READ_TIMEOUT, TimeUnit.MILLISECONDS))
    					.addHandlerLast(new WriteTimeoutHandler(WRITE_TIMEOUT, TimeUnit.MILLISECONDS))
    			);
    			//.wiretap("reactor.netty.http.client.HttpClient", io.netty.handler.logging.LogLevel.INFO);
    	
    	// Configure exchange strategies with larger buffer size 
    	ExchangeStrategies strategies = ExchangeStrategies.builder()
    			.codecs(configurer -> {
    				configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE);
    			})
    			.build();    	
    	
    	// Build the WebClient with the configured HttpClient, exchange strategies, and filters
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(strategies)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .filter(loggingFilter())
            .filter(errorHandlingFilter())
            .filter(rateLimitFilter());
    }
    
    /**
     * Logging filter for request/response
     */
    private ExchangeFilterFunction loggingFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (LOGGING_ENABLED && LOG_REQUESTS) {
                log.debug("Composing request: {} {}", clientRequest.method(), clientRequest.url());
                log.trace("Request Headers: {}", clientRequest.headers());
            }
            return Mono.just(clientRequest);
        });
    }
    
    /**
     * Error handling filter
     */
    private ExchangeFilterFunction errorHandlingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                log.error("Received error response: Status {}", clientResponse.statusCode());
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            log.error("Error Response Body: {}", errorBody);
                            return Mono.just(clientResponse);
                        });
            }
            
            if (LOGGING_ENABLED && LOG_RESPONSES) {
                log.debug("Received response: Status {}", clientResponse.statusCode());
            }
            
            return Mono.just(clientResponse);
        });
    }
    
    /**
     * Simple rate limiting filter (production should use Redis-based rate limiting)
     */
    private ExchangeFilterFunction rateLimitFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            // TODO: Implement proper rate limiting with Redis or external service
            // For now, just log
            if (LOGGING_ENABLED) {
                log.trace("Rate limit check for request: {}", clientRequest.url());
            }
            return Mono.just(clientRequest);
        });
    }
}
