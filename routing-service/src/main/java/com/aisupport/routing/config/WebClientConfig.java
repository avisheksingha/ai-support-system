package com.aisupport.routing.config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction;
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

@Configuration
@Slf4j
public class WebClientConfig {
    
    @Value("${webclient.timeout.connect:5000}")
    private Integer connectTimeout;
    
    @Value("${webclient.timeout.read:30000}")
    private Integer readTimeout;
    
    @Value("${webclient.timeout.write:10000}")
    private Integer writeTimeout;
    
    private static final int MAX_IN_MEMORY_SIZE = 10 * 1024 * 1024; // 10MB

    @Bean
    @LoadBalanced
    WebClient.Builder loadBalancedWebClientBuilder(LoadBalancedExchangeFilterFunction lbFunction) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .responseTimeout(Duration.ofMillis(readTimeout))
                .doOnConnected(conn -> 
                    conn.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(writeTimeout, TimeUnit.MILLISECONDS))
                ).wiretap("reactor.netty.http.client.HttpClient", io.netty.handler.logging.LogLevel.INFO);
    	
    	// Configure exchange strategies with larger buffer size 
    	ExchangeStrategies strategies = ExchangeStrategies.builder()
    			.codecs(configurer -> {
    				configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE);
    			})
    			.build();
        
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest())
                .filter(logResponse())
                .filter(errorHandlingFilter())
                .filter(lbFunction); // enables service name resolution via Spring Cloud LoadBalancer
    }
    
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("WebClient Request: {} {}", 
                    clientRequest.method(), clientRequest.url());
            return Mono.just(clientRequest);
        });
    }
    
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("WebClient Response: Status {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }
    
    private ExchangeFilterFunction errorHandlingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            log.error("WebClient Error Response: {} - {}", 
                                    clientResponse.statusCode(), errorBody);
                            return Mono.just(clientResponse);
                        });
            }
            return Mono.just(clientResponse);
        });
    }
}