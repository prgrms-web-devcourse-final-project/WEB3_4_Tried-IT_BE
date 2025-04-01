package com.dementor.domain.chat.controller;

import com.dementor.domain.chat.dto.ChatMessageResponseDto;
import com.dementor.domain.chat.dto.ChatMessageSendDto;
import com.dementor.domain.chat.service.ChatMessageService;
import com.dementor.domain.chat.service.ChatRoomService;
import com.dementor.global.jwt.JwtParser;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final SimpMessageSendingOperations messagingTemplate;
    private final JwtParser jwtParser;

    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageSendDto dto, @Header("Authorization") String token) {
        try {
            Long memberId = jwtParser.getMemberId(token);
            String nickname = jwtParser.getNickname(token);

            ChatMessageResponseDto response = chatMessageService.handleMessage(dto, memberId, nickname);
            messagingTemplate.convertAndSend("/sub/chat/room/" + dto.getApplymentId(), response);
        }
        catch (Exception e) {
            System.err.println("WebSocket 인증 오류: " + e.getMessage());
        }
    }
}



// -메시지 보낼 때 예외처리 (JWT가 잘못되었을 경우 예외처리)
//@MessageMapping("/chat/message")
//public void sendMessage(ChatMessageSendDto dto, @Header("Authorization") String token) {
//    try {
//        Long memberId = jwtParser.getmemberId(token);
//        String nickname = jwtParser.getNickname(token);
//
//        ChatMessageResponseDto response = chatService.handleMessage(dto, memberId, nickname);
//        messagingTemplate.convertAndSend("/sub/chat/room/" + dto.getApplymentId(), response);
//    } catch (Exception e) {
//        // 예: 인증 실패, 응답 생략 등
//        System.err.println("WebSocket 인증 오류: " + e.getMessage());
//    }
//}
