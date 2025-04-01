package com.dementor.global.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 연결할 웹소켓 엔드포인트
        registry.addEndpoint("/ws-stomp") // 또는 "/chat/ws-stomp"로 바꿔도 OK
                .setAllowedOriginPatterns("*") // CORS 허용 (운영 시 도메인 지정 추천)
                .withSockJS(); // SockJS fallback 지원
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 서버 → 클라이언트로 메시지를 보낼 경로(prefix)
        registry.enableSimpleBroker("/sub");

        // 클라이언트 → 서버로 메시지를 보낼 경로(prefix)
        registry.setApplicationDestinationPrefixes("/pub");
    }
}
