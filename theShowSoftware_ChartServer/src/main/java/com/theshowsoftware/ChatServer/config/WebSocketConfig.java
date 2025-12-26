package com.theshowsoftware.ChatServer.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     WebSocket 연결 Endpoint 설정
     **/
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/udws")
                .setAllowedOriginPatterns("https://*", "http://*")
                .withSockJS();
    }

    /**
     STOMP 프로토콜 기반의 메시지 브로커 설정
     **/
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub", "/user"); // 구독
        registry.setApplicationDestinationPrefixes("/pub"); // 발행
        registry.setUserDestinationPrefix("/user"); // User Destination Prefix
    }
}