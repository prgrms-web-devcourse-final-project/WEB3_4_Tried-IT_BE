//package com.dementor.domain.chat.service;
//
//import com.dementor.domain.chat.dto.ChatMessageSendDto;
//import com.dementor.domain.chat.dto.ChatMessageResponseDto;
//import com.dementor.domain.chat.dto.ChatMessageSliceDto;
//import com.dementor.domain.chat.entity.ChatMessage;
//import com.dementor.domain.chat.entity.ChatRoom;
//import com.dementor.domain.chat.entity.MessageType;
//import com.dementor.domain.chat.repository.ChatMessageRepository;
//import com.dementor.domain.chat.repository.ChatRoomRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.time.ZoneId;
//import java.time.ZonedDateTime;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class ChatService {
//
//    private final ChatRoomRepository chatRoomRepository;
//    private final ChatMessageRepository chatMessageRepository;
//
//
//
//    public ChatMessageResponseDto handleMessage(ChatMessageSendDto dto, Long memberId, String nickname) {
//        ChatRoom chatRoom = chatRoomRepository.findByApplymentId(dto.getApplymentId())
//                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));
//
//        ChatMessage message = new ChatMessage();
//        message.setChatRoom(chatRoom);
//        message.setNickname(nickname);
//        message.setMemberId(memberId);
//        message.setContent(dto.getType() == MessageType.MESSAGE ? dto.getMessage() : null);
//
//        chatMessageRepository.save(message);
//
//        return new ChatMessageResponseDto(
//                dto.getType(),   //enum값을 문자열로 바꾸기 위해 .name 씀
//                dto.getApplymentId(),
//                memberId,
//                nickname,
//                message.getContent(), // 저장된 메시지
//                message.getCreatedAt().atZone(ZoneId.of("Asia/Seoul"))
//        );
//    }
//
//
//
//
//    public List<ChatMessageResponseDto> getMessages(Long applymentId) {
//        ChatRoom chatRoom = chatRoomRepository.findByApplymentId(applymentId)
//                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));
//
//        return chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoom.getChatRoomId())
//                .stream()
//                .map(msg -> new ChatMessageResponseDto(
//                        msg.getType(), // MESSAGE, ENTER, EXIT 등
//                        applymentId,
//                        msg.getMemberId(),
//                        msg.getNickname(),
//                        msg.getContent(),
//                        msg.getCreatedAt().atZone(ZoneId.of("Asia/Seoul"))
//                ))
//                .toList();
//    }
//
//    public ChatMessageSliceDto getMessages(Long chatRoomId, Long beforeMessageId, int size) {
//        List<ChatMessage> messages;
//
//        if (beforeMessageId != null) {
//            messages = chatMessageRepository
//                    .findTop20ByChatRoom_ChatRoomIdAndChatMessageIdLessThanOrderByChatMessageIdDesc(chatRoomId, beforeMessageId);
//        } else {
//            messages = chatMessageRepository
//                    .findTop20ByChatRoom_ChatRoomIdOrderByChatMessageIdDesc(chatRoomId);
//        }
//
//        List<ChatMessageResponseDto> dtoList = messages.stream()
//                .map(m -> new ChatMessageResponseDto(
//                        m.getType(),
//                        m.getChatRoom().getApplymentId(),
//                        m.getMemberId(),
//                        m.getNickname(),
//                        m.getContent(),
//                        m.getCreatedAt().atZone(ZoneId.of("Asia/Seoul"))
//                ))
//                .toList();
//
//        Long nextCursor = messages.isEmpty() ? null : messages.get(messages.size() - 1).getChatMessageId();
//
//        return new ChatMessageSliceDto(dtoList, dtoList.size() == size, nextCursor);
//    }
//
//
//
//}
//


package com.dementor.domain.chat.service;

import com.dementor.domain.chat.dto.ChatRoomResponseDto;
import com.dementor.domain.chat.entity.ChatMessage;
import com.dementor.domain.chat.entity.ChatRoom;
import com.dementor.domain.chat.entity.RoomType;
import com.dementor.domain.chat.repository.ChatMessageRepository;
import com.dementor.domain.chat.repository.ChatRoomRepository;
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


    private static final ZoneId KST = ZoneId.of("Asia/Seoul"); //  시간대 상수화




    //채팅방 조회+ 마지막 메시지 조회 + 상대 닉네임 매핑
    @Transactional(readOnly = true)
    public List<ChatRoomResponseDto> getMyChatRooms(Long memberId) {
        List<ChatRoom> rooms = chatRoomRepository.findByMemberId(memberId); // 사용자 채팅방 목록 조회

        return rooms.stream().map(room -> {
            // 1. 마지막 메시지 가져오기
            ChatMessage lastMessage = chatMessageRepository
                    .findTop1ByChatRoom_ChatRoomIdOrderByCreatedAtDesc(room.getChatRoomId())
                    .stream().findFirst().orElse(null);

            // 2. 닉네임 매핑 로직 (연관된 Member에서 닉네임 직접 조회)
            String nickname;
            if (room.getRoomType() == RoomType.ADMIN_CHAT) {
                nickname = "관리자";
            } else {
                Long opponentId = !room.getMemberId().equals(memberId) ? room.getMemberId() : null;
                nickname = opponentId != null
                        ? room.getMemberNickname() //
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





//**멤버 서비스에   이부분 추가
// 추가된 닉네임 조회 메서드
//    public String getNicknameById(Long memberId) {
//        return memberRepository.findById(memberId)
//                .map(Member::getNickname)
//                .orElse("알 수 없음");

