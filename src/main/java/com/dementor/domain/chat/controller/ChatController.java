package com.dementor.domain.chat.controller;

import com.dementor.domain.chat.dto.ChatMessageSliceDto;
import com.dementor.domain.chat.dto.ChatRoomResponseDto;
import com.dementor.domain.chat.service.ChatMessageService;

import com.dementor.domain.chat.service.ChatRoomService;
import com.dementor.global.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.JwtParser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    private final JwtTokenProvider jwtTokenProvider;


    // 커서 기반 메시지 조회 (최신 → 오래된 순, 무한스크롤)
    @GetMapping("/room/{chatRoomId}/messages")
    public ResponseEntity<ChatMessageSliceDto> getMessagesWithCursor(
            @PathVariable Long chatRoomId,
            @RequestParam(required = false) Long beforeMessageId,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(chatMessageService.getMessages(chatRoomId, beforeMessageId, size));
    }


    // 채팅방 목록조회   /api/chat/rooms
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponseDto>> getMyChatRooms(@RequestHeader("Authorization") String token) {
        Long memberId = jwtTokenProvider.getMemberId(token);
        return ResponseEntity.ok(chatRoomService.getMyChatRooms(memberId));
    }


//    //  예전 전체 메시지 조회 API (선택적으로 유지 가능)
//    @GetMapping("/{applymentId}")
//    public ResponseEntity<List<ChatMessageResponseDto>> getMessages(@PathVariable Long applymentId) {
//        return ResponseEntity.ok(chatService.getAllMessagesOldOrder(applymentId));
//    }
}
