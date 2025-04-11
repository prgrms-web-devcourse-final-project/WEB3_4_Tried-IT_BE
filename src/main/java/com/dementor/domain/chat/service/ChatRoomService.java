package com.dementor.domain.chat.service;

import com.dementor.domain.chat.dto.ChatRoomResponseDto;
import com.dementor.domain.chat.entity.ChatMessage;
import com.dementor.domain.chat.entity.ChatRoom;
import com.dementor.domain.chat.entity.RoomType;
import com.dementor.domain.chat.repository.ChatMessageRepository;
import com.dementor.domain.chat.repository.ChatRoomRepository;
import com.dementor.domain.admin.entity.Admin;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;

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
    public ChatRoomResponseDto createAdminChatRooms(Admin admin, Member member) {
        ChatRoom room = ChatRoom.builder()
                .roomType(RoomType.ADMIN_CHAT)
                .adminId(admin.getId())
                .memberId(member.getId())
//                .targetNickname("관리자") // 하드코딩된 관리자 닉네임
                .build();

        chatRoomRepository.save(room);

        return toDto(room, admin.getId());  //dto 매개변수 viewerid

    }
//--------------------------채팅방 목록 조회--------------------------------------

    // 사용자(memberId) 기준 참여 중인 모든 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<ChatRoomResponseDto> getAllMyChatRooms(Long memberId) {
        List<ChatRoom> mentoringRooms = chatRoomRepository.findMentoringChatRoomsByMemberId(memberId);
        List<ChatRoom> adminRooms = chatRoomRepository.findAdminChatRoomsByMemberId(memberId);

        return List.of(mentoringRooms, adminRooms).stream()
                .flatMap(List::stream)
                .map(room -> toDto(room, memberId)) // viewerId 넘기기
                .toList();
    }


    // 관리자(adminId)기준 참여중인 모든 채팅방 조회
    @Transactional(readOnly = true)
    public List<ChatRoomResponseDto> getAllMyAdminChatRooms(Long adminId) {
        List<ChatRoom> rooms = chatRoomRepository.findAdminChatRoomsByAdminId(adminId);
        return rooms.stream().map(room -> toDto(room, adminId)).toList();
    }

//---------------------채팅방 상세 조회(viewerId,viewerType 매칭) --------------------------------------
@Transactional(readOnly = true)
public ChatRoomResponseDto getChatRoomDetail(Long chatRoomId, Long viewerId, String viewerType) {
    ChatRoom room = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));  //roomId 존재안함

    if (room.getRoomType() == RoomType.MENTORING_CHAT) {
        // viewerType이 member가 아닐 경우 차단
        if (!"member".equals(viewerType)) {
            throw new SecurityException("멘토링 채팅방은 member만 접근할 수 있습니다.");
        }
       //  viewerId가 mentorId 또는 menteeId와 일치여부
        if (!viewerId.equals(room.getMentorId()) && !viewerId.equals(room.getMenteeId())) {
            throw new SecurityException("해당 채팅방에 접근할 수 없습니다.");
        }

    } else if (room.getRoomType() == RoomType.ADMIN_CHAT) {
        if ("member".equals(viewerType) && !viewerId.equals(room.getMemberId())) {
            throw new SecurityException("해당 채팅방에 접근할 수 없습니다.");
        }
        if ("admin".equals(viewerType) && !viewerId.equals(room.getAdminId())) {
            throw new SecurityException("해당 채팅방에 접근할 수 없습니다.");
        }
    }

    return toDto(room, viewerId);
}





//-----------------------------닉네임관련-----------------------------------
    // ChatRoomResponseDto 변환 & 실시간 닉네임 조회
    private ChatRoomResponseDto toDto(ChatRoom room, Long viewerId) {
        List<ChatMessage> messages = chatMessageRepository
                .findTop1ByChatRoom_ChatRoomIdOrderBySentAtDesc(room.getChatRoomId());
        ChatMessage lastMessage = messages.isEmpty() ? null : messages.get(0);

        String targetNickname = getTargetNickname(room, viewerId);

        return new ChatRoomResponseDto(
                room.getChatRoomId(),
                room.getRoomType(),
                lastMessage != null ? lastMessage.getContent() : null,
                lastMessage != null ? lastMessage.getSentAt().atZone(ZoneId.of("Asia/Seoul")) : null,
                targetNickname
        );
    }


    // 자신의 입장에서 상대방 닉네임 반환 (캐시를 이용해서 닉네임 조회)
    public String getTargetNickname(ChatRoom room, Long viewerId) {
        if (room.getRoomType() == RoomType.MENTORING_CHAT) {
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
            if (viewerId.equals(room.getAdminId())) {
                return memberRepository.findById(room.getMemberId())
                        .map(Member::getNickname)
                        .orElse("알 수 없음");
            } else {
                return "관리자"; // 사용자 입장에서 → '관리자'
            }
        }

        return "알 수 없음";
    }
}
