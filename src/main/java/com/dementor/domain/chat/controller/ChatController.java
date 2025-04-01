package com.dementor.domain.chat.controller;

import com.dementor.domain.chat.dto.ChatMessageResponseDto;
import com.dementor.domain.chat.dto.ChatMessageSliceDto;
import com.dementor.domain.chat.dto.ChatRoomResponseDto;
import com.dementor.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.dementor.global.jwt.JwtParser;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final JwtParser jwtParser; // 주입



    // 커서 기반 메시지 조회 (최신 → 오래된 순, 무한스크롤)
    @GetMapping("/room/{chatRoomId}/messages")
    public ResponseEntity<ChatMessageSliceDto> getMessagesWithCursor(
            @PathVariable Long chatRoomId,
            @RequestParam(required = false) Long beforeMessageId,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(chatService.getMessages(chatRoomId, beforeMessageId, size));
    }


    // 채팅방 목록조회   /api/chat/rooms
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponseDto>> getMyChatRooms(@RequestHeader("Authorization") String token) {
        Long memberId = jwtParser.getMemberId(token); // jwt✅
        return ResponseEntity.ok(chatService.getMyChatRooms(memberId));
    }


//    //  예전 전체 메시지 조회 API (선택적으로 유지 가능)
//    @GetMapping("/{applymentId}")
//    public ResponseEntity<List<ChatMessageResponseDto>> getMessages(@PathVariable Long applymentId) {
//        return ResponseEntity.ok(chatService.getAllMessagesOldOrder(applymentId));
//    }
}
