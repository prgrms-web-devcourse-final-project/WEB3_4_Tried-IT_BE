package com.dementor.domain.chat.service;

import com.dementor.domain.apply.entity.Apply;
import com.dementor.domain.apply.repository.ApplyRepository;

import com.dementor.domain.chat.dto.ChatRoomResponseDto;
import com.dementor.domain.chat.entity.ChatRoom;
import com.dementor.domain.chat.entity.RoomType;
import com.dementor.domain.chat.repository.ChatRoomRepository;

import com.dementor.domain.chat.entity.ChatMessage;
import com.dementor.domain.chat.repository.ChatMessageRepository;

import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.repository.MemberRepository;

import com.dementor.domain.admin.entity.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    private final ApplyRepository applyRepository;
    private final MemberRepository memberRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");


    //채팅방 생성---------------------------------------------------------------------

    //  멘토 & 멘티 각각에게 채팅방 생성
    @Transactional
    public void createMentoringChatRooms(Long applymentId, Member mentor, Member mentee) {
        ChatRoom mentorRoom = new ChatRoom();
//        mentorRoom.setApplymentId(applymentId);
        mentorRoom.setRoomType(RoomType.MENTORING_CHAT);
        mentorRoom.setMember(mentor); //사용자 멘토
        mentorRoom.setTargetNickname(mentee.getNickname()); // 상대방 사용자 멘티 저장
        chatRoomRepository.save(mentorRoom);

        ChatRoom menteeRoom = new ChatRoom();
//        menteeRoom.setApplymentId(applymentId);
        menteeRoom.setRoomType(RoomType.MENTORING_CHAT);
        menteeRoom.setMember(mentee);  // 사용자 멘티
        menteeRoom.setTargetNickname(mentor.getNickname()); // 상대방 사용자 멘토 저장
        chatRoomRepository.save(menteeRoom);
    }

    // 관리자 & 멤버 각각에게 채팅방 생성
    @Transactional
    public void createAdminChatRooms(Admin admin, Member member) {
        // 사용자 기준 채팅방 생성
        ChatRoom userRoom = new ChatRoom();
        userRoom.setRoomType(RoomType.ADMIN_CHAT);
        userRoom.setMember(member); // 사용자
        userRoom.setAdmin(admin); //상대방 사용자
        chatRoomRepository.save(userRoom);

        // 관리자 기준 채팅방 생성
        ChatRoom adminRoom = new ChatRoom();
        adminRoom.setRoomType(RoomType.ADMIN_CHAT);
        adminRoom.setAdmin(admin); //관리자
        adminRoom.setMember(member); //상대방 사용자
        chatRoomRepository.save(adminRoom);
    }

    //-----------------------------------------------------------------------------


    //findByMember_Id(memberId)로 검색하면 관리자든 멘토링이든 내가 포함된 모든 채팅방이 조회됨
    //findByAdmin_AdminId(adminId) 조회. 관리자쪽에서 관리자 챗 채팅방 조회 가능
    // 채팅방 목록 조회 + 마지막 메시지 + 상대방 닉네임 ----db에서 가져옴, 상대방 닉네임 필요. 토큰에서 가져오면 내 닉네임만

    // 사용자 기준 모든 채팅방 조회
    @Transactional(readOnly = true)
    public List<ChatRoomResponseDto> getAllMyChatRooms(Long memberId) {
        List<ChatRoom> rooms = chatRoomRepository.findByMember_Id(memberId);

        return rooms.stream().map(room -> {
            // 상대방 닉네임 구분 처리
            String nickname = room.getRoomType() == RoomType.ADMIN_CHAT
                    ? "관리자"  //admin 챗이면 상대방(관리자) 닉네임은 관리자
                    : room.getMember().getNickname(); // 어드민 챗 아니면 상대방(멤버)의 닉네임 (DB 조회)

            return toDto(room, nickname);
        }).toList();
    }

    // 관리자 기준 채팅방 조회
    @Transactional(readOnly = true)
    public List<ChatRoomResponseDto> getAllMyAdminChatRooms(Long adminId) {
        List<ChatRoom> rooms = chatRoomRepository.findByAdmin_AdminId(adminId);

        return rooms.stream().map(room -> {
            // 상대방 닉네임: 항상 Member 기준
            String nickname = room.getMember().getNickname(); // 상대방 사용자의 닉네임
            return toDto(room, nickname);
        }).toList();
    }

    // 공통 DTO
    private ChatRoomResponseDto toDto(ChatRoom room, String nickname) {
        List<ChatMessage> messages = chatMessageRepository
                .findTop1ByChatRoom_ChatRoomIdOrderBySentAtDesc(room.getChatRoomId());

        ChatMessage lastMessage = messages.isEmpty() ? null : messages.get(0);


        return new ChatRoomResponseDto(
                room.getChatRoomId(),
//                room.getApplymentId(),
                room.getRoomType(),
                nickname,
                lastMessage != null ? lastMessage.getContent() : null,
                lastMessage != null ? lastMessage.getSentAt() : null,
                room.getTargetNickname()

        );
    }
}