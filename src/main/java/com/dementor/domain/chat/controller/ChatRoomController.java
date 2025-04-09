package com.dementor.domain.chat.controller;

import com.dementor.domain.admin.entity.Admin;
import com.dementor.domain.admin.repository.AdminRepository;
import com.dementor.domain.chat.dto.ChatRoomResponseDto;
import com.dementor.domain.chat.service.ChatMessageService;

import com.dementor.domain.chat.service.ChatRoomService;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatRoomController {


    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    private final MemberRepository memberRepository;
    private final AdminRepository adminRepository;


    // 멘토링 채팅방은 별도 api 필요없음( ApplyService.createApply() 내부에서 채팅방이 자동으로 생성)
    //ApplyService에서 apply 저장 후, mentor, mentee를 DB에서 꺼내서 chatRoomService.createMentoringChatRooms(...) 로 2번 저장까지 완료
    //관리자 챗 채팅방 생성
    @PostMapping("/admin-room")
    public ResponseEntity<ChatRoomResponseDto> createAdminChatRoom(
            @RequestParam Long adminId,
            @RequestParam Long memberId
    ) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));


        ChatRoomResponseDto room = chatRoomService.createAdminChatRooms(admin, member); //  반환값 받도록 변경


        return ResponseEntity.ok(room);
    }


    //멤버가 자신의 채팅방목록 조회
    @GetMapping("/member/rooms")
    public ResponseEntity<List<ChatRoomResponseDto>> getMyRoomsAsMember(
            @RequestParam Long memberId
    ) {
        return ResponseEntity.ok(chatRoomService.getAllMyChatRooms(memberId));
    }

    //관리자가 자신의 채팅방목록 조회
    @GetMapping("/admin/rooms")
    public ResponseEntity<List<ChatRoomResponseDto>> getMyRoomsAsAdmin(
            @RequestParam Long adminId
    ) {
        return ResponseEntity.ok(chatRoomService.getAllMyAdminChatRooms(adminId));
    }


    // ---------------------채팅방 상세 조회--------------------------------------
    @GetMapping("/room/{chatRoomId}")
    public ResponseEntity<ChatRoomResponseDto> getChatRoomDetail(
            @PathVariable Long chatRoomId,
            @RequestParam Long viewerId,
            @RequestParam String viewerType
    ) {
        ChatRoomResponseDto response = chatRoomService.getChatRoomDetail(chatRoomId, viewerId, viewerType);
        return ResponseEntity.ok(response);
    }

}

////---------------------메시지조회----------------------
//    // 채팅방 메시지 조회 API (커서 기반, 최신순 → 오래된순)
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
