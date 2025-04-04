//WebSocket을 통해 들어오는 STOMP 메시지의 인증을 처리하는 필터 역할
//클라이언트가 /pub/chat/message 같은 경로로 메시지를 보낼 때, JWT 토큰이 유효한지 검사

package com.dementor.global.security.websocket;

import com.dementor.global.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        String token = null;

        // CONNECT 요청일 때만 토큰 검증
        if (StompCommand.CONNECT.equals(command)) {
            token = accessor.getFirstNativeHeader("Authorization");

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // "Bearer " 제거
            }

            log.info(" WebSocket CONNECT - token: {}", token);

            if (token == null || !jwtTokenProvider.validateAccessToken(token)) {
                log.warn("WebSocket 인증 실패: 토큰 없음 또는 유효하지 않음");
                throw new JwtException("WebSocket 연결: JWT 인증 실패");
            }

            // 인증 성공 시 사용자 정보 저장 (선택)
            Long memberId = jwtTokenProvider.getMemberId(token);
            accessor.setUser(() -> String.valueOf(memberId)); // Principal로 사용 가능
        }

        return message;
    }
}
