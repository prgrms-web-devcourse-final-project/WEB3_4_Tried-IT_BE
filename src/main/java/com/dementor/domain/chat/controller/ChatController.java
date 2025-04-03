package com.dementor.domain.chat.controller;

import com.dementor.domain.admin.entity.Admin;
import com.dementor.domain.admin.repository.AdminRepository;
import com.dementor.domain.chat.dto.ChatMessageSliceDto;
import com.dementor.domain.chat.dto.ChatRoomResponseDto;
import com.dementor.domain.chat.service.ChatMessageService;

import com.dementor.domain.chat.service.ChatRoomService;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.repository.MemberRepository;
import com.dementor.global.security.jwt.JwtTokenProvider;
import com.dementor.global.security.cookie.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
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

    private final MemberRepository memberRepository;
    private final AdminRepository adminRepository;


    // 멘토링 채팅방은 별도 api 필요없음( ApplyService.createApply() 내부에서 채팅방이 자동으로 생성)
    //ApplyService에서 apply 저장 후, mentor, mentee를 DB에서 꺼내서 chatRoomService.createMentoringChatRooms(...) 로 2번 저장까지 완료
    //관리자 챗 채팅방 생성
    @PostMapping("/admin-room")
    public ResponseEntity<Void> createAdminChatRoom(
            @RequestParam Long adminId,
            @RequestParam Long memberId
    ) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        chatRoomService.createAdminChatRooms(admin, member); // 서비스 메서드 그대로 사용
        return ResponseEntity.ok().build();
    }


    //사용자 채팅방 목록 조회 (멤버 또는 관리자)
    // memberId 또는 adminId 중 하나를 파라미터로 전달받아 채팅방 목록 조회

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponseDto>> getMyChatRooms(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long adminId
    ) {
        if (memberId != null) {
            return ResponseEntity.ok(chatRoomService.getAllMyChatRooms(memberId));
        } else if (adminId != null) {
            return ResponseEntity.ok(chatRoomService.getAllMyAdminChatRooms(adminId));
        } else {
            throw new IllegalArgumentException("memberId 또는 adminId 중 하나는 필수입니다.");
        }
    }


}










//    // 커서 기반 메시지 조회 (최신 → 오래된 순, 무한스크롤)
//    @GetMapping("/room/{chatRoomId}/messages")
//    public ResponseEntity<ChatMessageSliceDto> getMessagesWithCursor(
//            @PathVariable Long chatRoomId,
//            @RequestParam(required = false) Long beforeMessageId,
//            @RequestParam(defaultValue = "20") int size
//    ) {
//        return ResponseEntity.ok(chatMessageService.getMessages(chatRoomId, beforeMessageId, size));
//    }



//--------------------------------------------------
//    //  예전 전체 메시지 조회 API (선택적으로 유지 가능)
//    @GetMapping("/{applymentId}")
//    public ResponseEntity<List<ChatMessageResponseDto>> getMessages(@PathVariable Long applymentId) {
//        return ResponseEntity.ok(chatService.getAllMessagesOldOrder(applymentId));
//    }
//}
