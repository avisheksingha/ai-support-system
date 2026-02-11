package com.aisupport.analysis.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Slf4j
@Configuration
public class GeminiConfig {

    private final GeminiPropertiesConfig props;

    public GeminiConfig(GeminiPropertiesConfig props) {
        this.props = props;
    }

    @Bean
    WebClient geminiWebClient() {
    	
    	// Create a connection provider with a custom name and max connections
        ConnectionProvider provider = ConnectionProvider.builder("gemini-pool")
                .maxConnections(props.getMaxConnections())
                .pendingAcquireTimeout(Duration.ofSeconds(10))
                .build();
        
        // Create and configure the HttpClient using the properties
        HttpClient httpClient = HttpClient.create(provider);
        props.configure(httpClient);

        return WebClient.builder()
                .baseUrl(props.getApiUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    // Log request method and URL
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(req -> {
            log.debug("Gemini request: {} {}", req.method(), req.url());
            return Mono.just(req);
        });
    }

    // Log response status code
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(resp -> {
            log.debug("Gemini response status: {}", resp.statusCode());
            return Mono.just(resp);
        });
    }
}
