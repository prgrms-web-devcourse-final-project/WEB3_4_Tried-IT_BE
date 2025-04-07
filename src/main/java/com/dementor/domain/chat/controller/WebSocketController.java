package com.dementor.domain.chat.controller;

import com.dementor.domain.chat.dto.ChatMessageSendDto;
import com.dementor.domain.chat.dto.ChatMessageResponseDto;
import com.dementor.domain.chat.entity.MessageType;
import com.dementor.domain.chat.entity.SenderType;
import com.dementor.domain.chat.service.ChatMessageService;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.repository.MemberRepository;
import com.dementor.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import org.springframework.messaging.simp.SimpMessagingTemplate; // ✅ 수정: SimpMessagingTemplate 임포트

import java.time.LocalDateTime;
import java.time.ZoneId;
@Slf4j

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final ChatMessageService chatMessageService;
//    private final SimpMessageSendingOperations messagingTemplate;
    private final SimpMessagingTemplate messagingTemplate; // Spring이 자동 생성해주는 구현체


    private final JwtTokenProvider jwtTokenProvider;      // JWT로 memberId 추출용
    private final MemberRepository memberRepository;      // 닉네임 조회용




    //---------------전송 & JWT에서 senderId, senderType 추출
    @MessageMapping("/chat/message")

    public void sendMessage(ChatMessageSendDto dto, @Header("Authorization") String token) {

        log.info("WebSocket 메시지 도착: {}", dto.getMessage());


        try {
            Long senderId;
            SenderType senderType;

            // JWT 토큰 분석 흐 관리자 또는 멤버 분기
            if (jwtTokenProvider.isAdminToken(token)) {
                senderId = jwtTokenProvider.getAdminId(token);
                senderType = SenderType.ADMIN;
            } else {
                senderId = jwtTokenProvider.getMemberId(token);
                senderType = SenderType.MEMBER;
            }

            log.info("senderId = {}, senderType = {}", senderId, senderType);

            // 메시지 처리 및 저장
            ChatMessageResponseDto response = chatMessageService.handleMessage(dto, senderId, senderType);

            // 구독자에게 브로드캐스트(메시지 전송)
            messagingTemplate.convertAndSend("/sub/chat/room/" + dto.getChatRoomId(), response);


            String destination = "/sub/chat/room/" + dto.getChatRoomId();
            log.info("메시지 전송 → {}", destination);


        } catch (Exception e) {
            System.err.println("WebSocket 메시지 처리 오류: " + e.getMessage());
        }
    }



}
