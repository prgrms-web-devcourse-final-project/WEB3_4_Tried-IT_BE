package com.dementor.domain.chat.controller;

import com.dementor.domain.admin.entity.Admin;
import com.dementor.domain.admin.repository.AdminRepository;
import com.dementor.domain.chat.dto.ChatRoomResponseDto;
//import com.dementor.domain.chat.service.ChatMessageService;

import com.dementor.domain.chat.service.ChatRoomService;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.dementor.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatRoomController {

	private final ChatRoomService chatRoomService;
//	private final ChatMessageService chatMessageService;

	private final MemberRepository memberRepository;
	private final AdminRepository adminRepository;

	// 멘토링 채팅방은 별도 api 필요없음( ApplyService.createApply() 내부에서 채팅방이 자동으로 생성)

	//관리자 챗 채팅방 생성
	@PostMapping("/admin-room")
	public ResponseEntity<ChatRoomResponseDto> createAdminChatRoom(
		@AuthenticationPrincipal CustomUserDetails userDetails

	) {
		Long memberId = userDetails.getId(); // 로그인된 사용자 ID 가져오기

		//채팅방 생성
		ChatRoomResponseDto room = chatRoomService.createAdminChatRooms(memberId); //  반환값 받도록 변경
		return ResponseEntity.ok(room);
	}


	//멤버가 자신의 채팅방목록 조회
	@GetMapping("/member/rooms")
	public ResponseEntity<List<ChatRoomResponseDto>> getMyRoomsAsMember(
			@AuthenticationPrincipal CustomUserDetails userDetails

	) {
		String authority = userDetails.getAuthorities().iterator().next().getAuthority();
		if ("ROLE_ADMIN".equals(authority)) {
			throw new SecurityException("관리자는 접근할 수 없습니다.");
		}
		Long memberId = userDetails.getId();  // 로그인한 사용자 ID 추출

		return ResponseEntity.ok(chatRoomService.getAllMyChatRooms(memberId));
	}


	//관리자가 자신의 채팅방목록 조회
	@GetMapping("/admin/rooms")
	public ResponseEntity<List<ChatRoomResponseDto>> getMyRoomsAsAdmin(
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		String authority = userDetails.getAuthorities().iterator().next().getAuthority();
		if (!"ROLE_ADMIN".equals(authority)) {
			throw new SecurityException("관리자만 접근할 수 있습니다.");
		}
		Long adminId = userDetails.getId();
		return ResponseEntity.ok(chatRoomService.getAllMyAdminChatRooms(adminId));
	}


	// 채팅방 상세조회
	@GetMapping("/room/{chatRoomId}")
	public ResponseEntity<ChatRoomResponseDto> getChatRoomDetail(
		@PathVariable Long chatRoomId,
		@AuthenticationPrincipal CustomUserDetails userDetails

	) {
		return ResponseEntity.ok(chatRoomService.getChatRoomDetail(chatRoomId, userDetails));
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
