package com.dementor.global.websocket;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

import com.dementor.global.security.jwt.JwtTokenProvider;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;

    public WebSocketConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;}


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 연결할 웹소켓 엔드포인트

        // 일반 WebSocket 연결 (SockJS 없이) - WebSocket King 등 외부 테스트용
        registry.addEndpoint("/ws-stomp")
                .addInterceptors(new JwtHandshakeInterceptor()) // ⬅ 인터셉터 등록
                .setAllowedOrigins("*");

        // SockJS fallback 포함 - 브라우저 클라이언트용
        registry.addEndpoint("/ws-stomp")
                .addInterceptors(new JwtHandshakeInterceptor()) // ⬅ 인터셉터 등록
                .setAllowedOrigins("*") // 브라우저 환경에서도 허용
                .setAllowedOriginPatterns("*") // 필요 시 origin 제한 가능
                .withSockJS();

        // ✅ 테스트용 인증 없는 WebSocket 엔드포인트 (WebSocket King 전용)
        registry.addEndpoint("/ws-test")
                .setAllowedOrigins("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 서버 → 클라이언트로 메시지를 보낼 경로(prefix)
        registry.enableSimpleBroker("/sub");

        // 클라이언트 → 서버로 메시지를 보낼 경로(prefix)
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new StompHandler(jwtTokenProvider));
    }
}