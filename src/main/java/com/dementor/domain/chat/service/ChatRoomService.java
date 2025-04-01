//
//package com.dementor.domain.chat.service;
//
//import com.dementor.domain.chat.dto.ChatRoomResponseDto;
//import com.dementor.domain.chat.entity.ChatMessage;
//import com.dementor.domain.chat.entity.ChatRoom;
//import com.dementor.domain.chat.entity.RoomType;
//import com.dementor.domain.chat.repository.ChatMessageRepository;
//import com.dementor.domain.chat.repository.ChatRoomRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.ZoneId;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class ChatRoomService {
//
//    private final ChatRoomRepository chatRoomRepository;
//    private final ChatMessageRepository chatMessageRepository;
//
//
//    private static final ZoneId KST = ZoneId.of("Asia/Seoul"); //  시간대 상수화
//
//
//
//
//    //채팅방 조회+ 마지막 메시지 조회 + 상대 닉네임 매핑
//    @Transactional(readOnly = true)
//    public List<ChatRoomResponseDto> getMyChatRooms(Long memberId) {
//        List<ChatRoom> rooms = chatRoomRepository.findByMember_Id(memberId); // 사용자 채팅방 목록 조회
//
//        return rooms.stream().map(room -> {
//            // 1. 마지막 메시지 가져오기
//            ChatMessage lastMessage = chatMessageRepository
//                    .findTop1ByChatRoom_ChatRoomIdOrderByCreatedAtDesc(room.getChatRoomId())
//                    .stream().findFirst().orElse(null);
//
//            // 2. 닉네임 매핑 로직 (연관된 Member에서 닉네임 직접 조회)
//            String nickname;
//            if (room.getRoomType() == RoomType.ADMIN_CHAT) {
//                nickname = "관리자";
//            } else {
//                Long opponentId = !room.getMemberId().equals(memberId) ? room.getMemberId() : null;
//                nickname = opponentId != null
//                        ? room.getMemberNickname() //
//                        : "알 수 없음";
//            }
//
//            return new ChatRoomResponseDto(
//                    room.getChatRoomId(),
//                    room.getApplymentId(),
//                    room.getRoomType(),
//                    nickname,
//                    lastMessage != null ? lastMessage.getContent() : null,
//                    lastMessage != null ? lastMessage.getCreatedAt() : null
//            );
//        }).toList();
//    }
//
//}
//
//
//
//
//
////**멤버 서비스에   이부분 추가
//// 추가된 닉네임 조회 메서드
////    public String getNicknameById(Long memberId) {
////        return memberRepository.findById(memberId)
////                .map(Member::getNickname)
////                .orElse("알 수 없음");
//


package com.dementor.domain.chat.service;

import com.dementor.domain.apply.repository.ApplyRepository;
import com.dementor.domain.chat.dto.ChatRoomResponseDto;
import com.dementor.domain.chat.entity.ChatMessage;
import com.dementor.domain.chat.entity.ChatRoom;
import com.dementor.domain.chat.entity.RoomType;
import com.dementor.domain.chat.repository.ChatMessageRepository;
import com.dementor.domain.chat.repository.ChatRoomRepository;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.repository.MemberRepository;
import com.dementor.domain.apply.entity.Apply;
import com.dementor.domain.apply.repository.ApplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    // ✅ 리팩토링을 위해 추가된 의존성
    private final ApplyRepository applyRepository;
    private final MemberRepository memberRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // 채팅방 생성 메서드
    @Transactional
    public Long createChatRoom(Long applymentId, Long myId) {
        Apply apply = applyRepository.findById(applymentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 신청이 존재하지 않습니다."));

        // 나와 상대방 구분
        Long mentorId = apply.getMentoringClass().getMember().getId(); // 수업 개설자
        Long menteeId = apply.getMember().getId(); // 신청자

        Long opponentId = !mentorId.equals(myId) ? mentorId : menteeId;

        Member opponent = memberRepository.findById(opponentId)
                .orElseThrow(() -> new IllegalArgumentException("상대방이 존재하지 않습니다."));

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setApplymentId(applymentId);
        chatRoom.setRoomType(RoomType.MENTORING_CHAT);
        chatRoom.setMember(opponent); // 항상 상대방만 저장

        chatRoomRepository.save(chatRoom);
        return chatRoom.getChatRoomId();
    }

    //채팅방 생성 메서드
@Transactional
public Long createChatRoom(Long applymentId, Long myId) {
    Apply apply = applyRepository.findById(applymentId)
            .orElseThrow(() -> new IllegalArgumentException("해당 신청이 존재하지 않습니다."));

    // 신청한 사람 (멘티)
    Member mentee = apply.getMember();

    // MentoringClass.member (멘토)는 접근할 수 없으므로,
    // 내가 멘티가 아니면 상대방은 멘티라고 가정
    if (mentee.getId().equals(myId)) {
        throw new IllegalArgumentException("현재 구조에선 멘토인 경우 상대방 정보 확인이 불가합니다.");
    }

    // 상대방 = 신청자(멘티)
    Member opponent = mentee;

    ChatRoom chatRoom = new ChatRoom();
    chatRoom.setApplymentId(applymentId);
    chatRoom.setRoomType(RoomType.MENTORING_CHAT);
    chatRoom.setMember(opponent); // 항상 상대방만 저장

    chatRoomRepository.save(chatRoom);
    return chatRoom.getChatRoomId();
}




    // 채팅방 목록 조회 + 마지막 메시지 + 닉네임 매핑
    @Transactional(readOnly = true)
    public List<ChatRoomResponseDto> getMyChatRooms(Long memberId) {
        List<ChatRoom> rooms = chatRoomRepository.findByMember_Id(memberId); // 사용자 채팅방 목록 조회

        return rooms.stream().map(room -> {
            // 마지막 메시지 가져오기
            ChatMessage lastMessage = chatMessageRepository
                    .findTop1ByChatRoom_ChatRoomIdOrderByCreatedAtDesc(room.getChatRoomId())
                    .stream().findFirst().orElse(null);

            // 닉네임 매핑 로직
            String nickname;
            if (room.getRoomType() == RoomType.ADMIN_CHAT) {
                nickname = "관리자";
            } else {
                Long opponentId = !room.getMemberId().equals(memberId) ? room.getMemberId() : null;
                nickname = opponentId != null
                        ? room.getMemberNickname() // 연관된 Member로부터 직접 닉네임 조회
                        : "알 수 없음";
            }

            return new ChatRoomResponseDto(
                    room.getChatRoomId(),
                    room.getApplymentId(),
                    room.getRoomType(),
                    nickname,
                    lastMessage != null ? lastMessage.getContent() : null,
                    lastMessage != null ? lastMessage.getCreatedAt() : null
            );
        }).toList();
    }
}
