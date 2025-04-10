package com.dementor.domain.chat.controller;

import com.dementor.domain.chat.dto.ChatMessageSendDto;
import com.dementor.domain.chat.dto.ChatMessageResponseDto;
import com.dementor.domain.chat.entity.SenderType;
import com.dementor.domain.chat.service.ChatMessageService;
import com.dementor.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms/{chatRoomId}/messages")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    // 1. 메시지 목록 조회 (before 기반)
    @GetMapping
    public ResponseEntity<List<ChatMessageResponseDto>> getMessages(
            @PathVariable Long chatRoomId,
            @RequestParam(required = false) Long beforeMessageId
    ) {
        List<ChatMessageResponseDto> messages = chatMessageService.getMessages(chatRoomId, beforeMessageId);
        return ResponseEntity.ok(messages);
    }


    // 2. 메시지 전송 (REST) 클->서버, 메시지 저장
    @PostMapping
    public ResponseEntity<ChatMessageResponseDto> sendMessage(
            @PathVariable Long chatRoomId,
            @RequestBody ChatMessageSendDto dto
    ) {
        dto.setChatRoomId(chatRoomId);
        ChatMessageResponseDto response = chatMessageService.sendMessage(dto);
        return ResponseEntity.ok(response);
    }


    // 3. 메시지 전송 (WebSocket), RabbitMQ 통해 서버->클(구독자)
    @MessageMapping("/chat/rooms/{chatRoomId}/messages/create")
    public void receiveMessageViaWebsocket(
            @DestinationVariable Long chatRoomId,
            ChatMessageSendDto dto
            // ,@AuthenticationPrincipal CustomUserDetails user // 로그인한 사용자 정보 자동 주입

    ) {
        dto.setChatRoomId(chatRoomId); // 채팅방 Id는 경로 변수로 주입

//        // sender 정보는 프론트에서 안 받고 백엔드에서 직접 설정
//        dto.setSenderId(user.getId());
//        dto.setSenderType(SenderType.valueOf(user.getRole().name()));

        chatMessageService.sendMessage(dto);
    }
}
