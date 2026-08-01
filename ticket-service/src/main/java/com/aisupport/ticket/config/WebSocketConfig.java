package com.aisupport.ticket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Simple memory broker for V1. Can be replaced with RabbitMQ/ActiveMQ for V2 Notification Service.
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Use native WebSocket (no SockJS). All modern browsers support this.
        // SockJS is avoided because it adds HTTP polling complexity, CORS issues,
        // and gateway routing problems with no real benefit in modern environments.
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*");
    }
}
