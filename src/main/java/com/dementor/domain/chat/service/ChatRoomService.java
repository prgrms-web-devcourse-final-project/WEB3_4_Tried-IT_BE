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


	//닉네임 캐시 저장 - (닉네임캐싱) 최초 1회만 DB 조회 후 메모리 캐시에서 꺼냄
	private final Map<Long, String> nicknameCache = new ConcurrentHashMap<>();

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
//	public ChatRoomResponseDto createAdminChatRooms(Admin admin, Member member) {
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
				.toList();
	}

	// 관리자(adminId)기준 참여중인 모든 채팅방 조회
	@Transactional(readOnly = true)
//	public List<ChatRoomResponseDto> getAllMyAdminChatRooms(Long adminId) {
	public List<ChatRoomResponseDto> getAllMyAdminChatRooms(CustomUserDetails userDetails) {

		String authority = userDetails.getAuthorities().iterator().next().getAuthority();

		if (!"ROLE_ADMIN".equals(authority)) {
			throw new SecurityException("관리자만 접근할 수 있습니다.");
		}

		Long adminId = userDetails.getId();  // 로그인된 관리자 ID
		List<ChatRoom> rooms = chatRoomRepository.findAdminChatRoomsByAdminId(adminId);
		return rooms.stream().map(room -> toDto(room, adminId, ViewerType.ADMIN)).toList();
	}

	//---------------------채팅방 상세 조회(viewerId,viewerType 매칭) --------------------------------------
	@Transactional(readOnly = true)
	public ChatRoomResponseDto getChatRoomDetail(Long chatRoomId, CustomUserDetails userDetails) {
		ChatRoom room = chatRoomRepository.findById(chatRoomId)
				.orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

		Long viewerId = userDetails.getId();
		String authority = userDetails.getAuthorities().iterator().next().getAuthority(); // ex: ROLE_MENTOR, ROLE_ADMIN
		ViewerType viewerType = "ROLE_ADMIN".equals(authority) ? ViewerType.ADMIN : ViewerType.MEMBER;

		if (room.getRoomType() == RoomType.MENTORING_CHAT) {
			// getRole()을 직접 호출하지 않고 authority 기반으로 판단
			if (!"ROLE_MENTOR".equals(authority) && !"ROLE_MENTEE".equals(authority)) {
				throw new SecurityException("멘토링 채팅방은 멘토 또는 멘티만 접근할 수 있습니다.");
			}

			if (!viewerId.equals(room.getMentorId()) && !viewerId.equals(room.getMenteeId())) {
				throw new SecurityException("해당 채팅방에 접근할 수 없습니다.");
			}

		} else if (room.getRoomType() == RoomType.ADMIN_CHAT) {
			if ("ROLE_ADMIN".equals(authority)) {
				if (!viewerId.equals(room.getAdminId())) {
					throw new SecurityException("해당 채팅방에 접근할 수 없습니다.");
				}
			} else {
				if (!viewerId.equals(room.getMemberId())) {
					throw new SecurityException("해당 채팅방에 접근할 수 없습니다.");
				}
			}
		}

		return toDto(room, viewerId, viewerType);
	}


	//-----------------------------닉네임관련-----------------------------------
	// ChatRoomResponseDto 변환 & 실시간 닉네임 조회
	private ChatRoomResponseDto toDto(ChatRoom room, Long viewerId, ViewerType viewerType) {
		List<ChatMessage> messages = chatMessageRepository
				.findTop1ByChatRoom_ChatRoomIdOrderBySentAtDesc(room.getChatRoomId());
		ChatMessage lastMessage = messages.isEmpty() ? null : messages.get(0);

		String targetNickname = getTargetNickname(room, viewerId, viewerType);

		return new ChatRoomResponseDto(
				room.getChatRoomId(),
				room.getRoomType(),
				lastMessage != null ? lastMessage.getContent() : null,
				lastMessage != null ? lastMessage.getSentAt().atZone(ZoneId.of("Asia/Seoul")) : null,
				targetNickname
		);
	}

	// 자신의 입장에서 상대방 닉네임 반환 (캐시를 이용해서 닉네임 조회)
	public String getTargetNickname(ChatRoom room, Long viewerId, ViewerType viewerType) {
		if (room.getRoomType() == RoomType.MENTORING_CHAT) {  //viewerType이 member라는 가정
			Long targetId = viewerId.equals(room.getMentorId())
					? room.getMenteeId()
					: room.getMentorId();

			// 캐시 적용: 처음만 DB에서 조회, 이후 캐시에서 가져옴
			return nicknameCache.computeIfAbsent(targetId, id ->
					memberRepository.findById(id)
							.map(Member::getNickname)
							.orElse("알 수 없음")
			);
		}

		// 관리자 채팅: viewer가 관리자면 → 상대 member 닉네임 조회
		if (room.getRoomType() == RoomType.ADMIN_CHAT) {
			if (viewerType == ViewerType.ADMIN) {
				if (viewerId.equals(room.getAdminId())) {

					return memberRepository.findById(room.getMemberId())
							.map(Member::getNickname)
							.orElse("알 수 없음");
				}
			} else if (viewerType == ViewerType.MEMBER) {
				if (viewerId.equals(room.getMemberId())) {
					// viewer가 이 방의 멤버일 경우 → 상대는 관리자
					return "관리자";
				}
			}
		}

		return "알 수 없음";
	}
}


