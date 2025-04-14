package com.dementor.domain.chat.service;

import com.dementor.domain.admin.repository.AdminRepository;
import com.dementor.domain.chat.dto.ChatRoomResponseDto;
import com.dementor.domain.chat.entity.ChatMessage;
import com.dementor.domain.chat.entity.ChatRoom;
import com.dementor.domain.chat.entity.RoomType;
import com.dementor.domain.chat.entity.ViewerType;
import com.dementor.domain.chat.repository.ChatMessageRepository;
import com.dementor.domain.chat.repository.ChatRoomRepository;
import com.dementor.domain.admin.entity.Admin;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.entity.UserRole;
import com.dementor.domain.member.repository.MemberRepository;

import com.dementor.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final MemberRepository memberRepository;
	private final AdminRepository adminRepository;


//	//닉네임 캐시 저장 - (닉네임캐싱) 최초 1회만 DB 조회 후 메모리 캐시에서 꺼냄
//	private final Map<Long, String> nicknameCache = new ConcurrentHashMap<>();

	// 멘토링 채팅방 생성 or //기존 채팅방 반환
	@Transactional
	public ChatRoom getOrCreateMentoringChatRoom(Long mentorId, Long menteeId) {

		//        // 이미 존재하는 채팅방이 있는지 확인
		//        List<ChatRoom> existingRooms = chatRoomRepository.findMentoringChatRoomsByMemberId(menteeId);
		//        for (ChatRoom room : existingRooms) {
		//            if (room.getMentorId().equals(mentorId) && room.getMenteeId().equals(menteeId)) {
		//                return room;
		//            }
		//        }

		// 새로운 채팅방 생성
		ChatRoom newRoom = ChatRoom.builder()
				.roomType(RoomType.MENTORING_CHAT)
				.mentorId(mentorId)
				.menteeId(menteeId)
				//                .targetNickname(mentorNickname) // 기본값 (멘티 기준)
				.build();

		return chatRoomRepository.save(newRoom);
	}

	// 관리자 채팅방 생성
	@Transactional
	public ChatRoomResponseDto createAdminChatRooms(Long memberId) {

		// 멤버 조회
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		// DB에서 고정 관리자 조회 (ID = 5L)
		Admin admin = adminRepository.findById(5L)
				.orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다."));

		Long fixedAdminId = admin.getId();

		// 기존 방 존재 여부 확인
		List<ChatRoom> existingRooms = chatRoomRepository.findAdminChatRoomByAdminIdAndMemberId(
				fixedAdminId, member.getId()
		);
		if (!existingRooms.isEmpty()) {
			return toDto(existingRooms.get(0), member.getId(), ViewerType.MEMBER); // 수정: viewerType 추가
		}
		// 새 채팅방 생성
		ChatRoom room = ChatRoom.builder()
				.roomType(RoomType.ADMIN_CHAT)
				.adminId(admin.getId())
				.memberId(member.getId())
				.build();
		chatRoomRepository.save(room);
		return toDto(room, member.getId(), ViewerType.MEMBER); // 수정: viewerType 추가
	}

	//--------------------------채팅방 목록 조회--------------------------------------

	// 사용자(memberId) 기준 참여 중인 모든 채팅방 목록 조회
	@Transactional(readOnly = true)
	public List<ChatRoomResponseDto> getAllMyChatRooms(Long memberId) {
		List<ChatRoom> mentoringRooms = chatRoomRepository.findMentoringChatRoomsByMemberId(memberId);
		List<ChatRoom> adminRooms = chatRoomRepository.findAdminChatRoomsByMemberId(memberId);

		return List.of(mentoringRooms, adminRooms).stream()
				.flatMap(List::stream)
				.map(room -> toDto(room, memberId, ViewerType.MEMBER )) // viewerId 넘기기
				.sorted((r1, r2) -> { // 최근 메시지 기준 정렬 추가
					if (r1.getLastMessageAt() == null) return 1;
					if (r2.getLastMessageAt() == null) return -1;
					return r2.getLastMessageAt().compareTo(r1.getLastMessageAt());
				})
				.toList();
	}

	// 관리자(adminId)기준 참여중인 모든 채팅방 조회
	@Transactional(readOnly = true)
	public List<ChatRoomResponseDto> getAllMyAdminChatRooms(Long adminId) {

		List<ChatRoom> rooms = chatRoomRepository.findAdminChatRoomsByAdminId(adminId);
		return rooms.stream()
				.map(room -> toDto(room, adminId, ViewerType.ADMIN))
				.sorted((r1, r2) -> { // 최근 메시지 기준 정렬 추가
					if (r1.getLastMessageAt() == null) return 1;
					if (r2.getLastMessageAt() == null) return -1;
					return r2.getLastMessageAt().compareTo(r1.getLastMessageAt());
				})
				.toList();
	}


	//---------------------채팅방 상세 조회(viewerId,viewerType 매칭) --------------------------------------

	@Transactional(readOnly = true)
	public ChatRoomResponseDto getChatRoomDetail(Long chatRoomId, CustomUserDetails userDetails) {
		ChatRoom room = chatRoomRepository.findById(chatRoomId)
				.orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

		Long viewerId = userDetails.getId();
		String authority = userDetails.getAuthorities().iterator().next().getAuthority(); // ex: ROLE_MENTOR, ROLE_ADMIN

		ViewerType viewerType;
		switch (authority) {
			case "ROLE_ADMIN" -> viewerType = ViewerType.ADMIN;
			case "ROLE_MENTOR", "ROLE_MENTEE" -> viewerType = ViewerType.MEMBER;
			default -> throw new SecurityException("알 수 없는 권한입니다.");
		}

		// 접근 권한 검증
		if (room.getRoomType() == RoomType.MENTORING_CHAT) {
			if (viewerType != ViewerType.MEMBER ||
					!(viewerId.equals(room.getMentorId()) || viewerId.equals(room.getMenteeId()))) {
				throw new SecurityException("멘토링 채팅방은 멘토 또는 멘티만 접근할 수 있습니다.");
			}
		} else if (room.getRoomType() == RoomType.ADMIN_CHAT) {
			if ((viewerType == ViewerType.ADMIN && !viewerId.equals(room.getAdminId())) ||
					(viewerType == ViewerType.MEMBER && !viewerId.equals(room.getMemberId()))) {
				throw new SecurityException("해당 채팅방에 접근할 수 없습니다.");
			}
		}

		return toDto(room, viewerId, viewerType);
	}


	//------------------------------ ChatRoomResponseDto 변환 -------------------------------------

	private ChatRoomResponseDto toDto(ChatRoom room, Long viewerId, ViewerType viewerType) {

		// 가장 최신 메시지 1개 가져옴
		List<ChatMessage> messages = chatMessageRepository
				.findTop1ByChatRoom_ChatRoomIdOrderBySentAtDesc(room.getChatRoomId());
		ChatMessage lastMessage = messages.isEmpty() ? null : messages.get(0);

		//상대방 닉네임 가져오기
		String targetNickname = getTargetNickname(room, viewerId, viewerType);
		//상대방 Id 가져오기
		Long targetId = getTargetId(room, viewerId, viewerType);

		return new ChatRoomResponseDto(
				room.getChatRoomId(),
				room.getRoomType(),
				lastMessage != null ? lastMessage.getContent() : null,
				lastMessage != null ? lastMessage.getSentAt().atZone(ZoneId.of("Asia/Seoul")) : null,
				targetNickname,
				targetId
		);
	}

//	----------------------------------상대방  Id(pk), 닉네임 관련-------------------------------------
	// 자신의 입장에서 상대방 닉네임 반환 (targetNickname 설정)
	public String getTargetNickname(ChatRoom room, Long viewerId, ViewerType viewerType) {
		Long targetId = getTargetId(room, viewerId, viewerType);

		// 멘토링 챗에서 viewerType은 MEMBER로 보장됨 (TargetId 내가 멘토면 상대 멘티Id, 멘티면 멘토Id)
		if (room.getRoomType() == RoomType.MENTORING_CHAT ||
				(room.getRoomType() == RoomType.ADMIN_CHAT && viewerType == ViewerType.ADMIN)) {
			return memberRepository.findById(targetId)
					.map(Member::getNickname)
					.orElse("회원 정보가 없습니다");
		}

		// ADMIN_CHAT이면서 viewer가 MEMBER인 경우 → 상대는 관리자
		if (room.getRoomType() == RoomType.ADMIN_CHAT && viewerType == ViewerType.MEMBER) {
			return "관리자";
		}

		return "알 수 없음";
	}


	private Long getTargetId(ChatRoom room, Long viewerId, ViewerType viewerType) {
		if (room.getRoomType() == RoomType.MENTORING_CHAT) {
			return viewerId.equals(room.getMentorId()) ? room.getMenteeId() : room.getMentorId();
		}
		if (room.getRoomType() == RoomType.ADMIN_CHAT) {
			return viewerType == ViewerType.ADMIN ? room.getMemberId() : room.getAdminId();
		}
		return null;
	}


}


