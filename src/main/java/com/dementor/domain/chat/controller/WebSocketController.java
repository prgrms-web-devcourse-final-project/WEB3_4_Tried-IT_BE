package com.dementor.domain.chat.controller;

import com.dementor.domain.chat.dto.ChatMessageSendDto;
import com.dementor.domain.chat.dto.ChatMessageResponseDto;
import com.dementor.domain.chat.entity.SenderType;
import com.dementor.domain.chat.service.ChatMessageService;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.repository.MemberRepository;
import com.dementor.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final ChatMessageService chatMessageService;
    private final SimpMessageSendingOperations messagingTemplate;
    private final JwtTokenProvider jwtTokenProvider;      // JWT로 memberId 추출용
    private final MemberRepository memberRepository;      // 닉네임 조회용

    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageSendDto dto, @Header("Authorization") String token) {
        try {
            Long senderId;
            SenderType senderType;

            // 관리자 또는 멤버 분기
            if (jwtTokenProvider.isAdminToken(token)) {
                senderId = jwtTokenProvider.getAdminId(token);
                senderType = SenderType.ADMIN;
            } else {
                senderId = jwtTokenProvider.getMemberId(token);
                senderType = SenderType.MEMBER;
            }

            // 메시지 처리
            ChatMessageResponseDto response = chatMessageService.handleMessage(dto, senderId, senderType);

            // 구독자에게 메시지 전송
            messagingTemplate.convertAndSend("/sub/chat/room/" + dto.getChatRoomId(), response);

        } catch (Exception e) {
            System.err.println("WebSocket 메시지 처리 오류: " + e.getMessage());
        }
    }
}
