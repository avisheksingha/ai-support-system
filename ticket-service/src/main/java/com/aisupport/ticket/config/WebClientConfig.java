package com.aisupport.ticket.config;

import java.time.Duration;

import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Bean
    WebClient.Builder loadBalancedWebClientBuilder(LoadBalancedExchangeFilterFunction lbFunction) {
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient()))
            .filter(lbFunction); // enables service name resolution via Spring Cloud LoadBalancer
    }

    private HttpClient httpClient() {
        return HttpClient.create()
            .responseTimeout(Duration.ofSeconds(60)) // read timeout
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000); // connect timeout
    }
}
