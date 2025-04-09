package com.dementor.domain.chat.controller;

import com.dementor.domain.chat.dto.ChatMessageSendDto;
import com.dementor.domain.chat.dto.ChatMessageResponseDto;
import com.dementor.domain.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms/{chatRoomId}/messages")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    // 1. 메시지 전송 (REST)
    @PostMapping
    public ResponseEntity<ChatMessageResponseDto> sendMessage(
            @PathVariable Long chatRoomId,
            @RequestBody ChatMessageSendDto dto
    ) {
        dto.setChatRoomId(chatRoomId);
        ChatMessageResponseDto response = chatMessageService.sendMessage(dto);
        return ResponseEntity.ok(response);
    }

    // 2. 메시지 목록 조회 (before 기반)
    @GetMapping
    public ResponseEntity<List<ChatMessageResponseDto>> getMessages(
            @PathVariable Long chatRoomId,
            @RequestParam(required = false) Long beforeMessageId
    ) {
        List<ChatMessageResponseDto> messages = chatMessageService.getMessages(chatRoomId, beforeMessageId);
        return ResponseEntity.ok(messages);
    }
}
