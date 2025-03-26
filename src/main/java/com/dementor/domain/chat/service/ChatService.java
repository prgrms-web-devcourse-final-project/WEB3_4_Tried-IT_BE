package com.dementor.domain.chat.service;

import com.dementor.domain.chat.dto.ChatMessageSendDto;
import com.dementor.domain.chat.dto.ChatMessageResponseDto;
import com.dementor.domain.chat.entity.ChatMessage;
import com.dementor.domain.chat.entity.ChatRoom;
import com.dementor.domain.chat.repository.ChatMessageRepository;
import com.dementor.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageResponseDto handleMessage(ChatMessageSendDto dto, Long userId, String nickname) {
        ChatRoom chatRoom = chatRoomRepository.findByApplymentId(dto.getApplymentId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        ChatMessage message = new ChatMessage();
        message.setChatRoom(chatRoom);
        message.setNickname(nickname);  // DB엔 nickname만 저장

        if (dto.getType() == ChatMessageSendDto.MessageType.MESSAGE) {
            message.setContent(dto.getMessage());
            chatMessageRepository.save(message);
        } else {
            message.setContent(dto.getType().name()); // ENTER/EXIT은 단순 알림 텍스트
        }

        return new ChatMessageResponseDto(
                dto.getType().name(),
                dto.getApplymentId(),
                userId,
                nickname,
                dto.getMessage(),
                ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        );
    }

    public List<ChatMessageResponseDto> getMessages(Long applymentId) {
        ChatRoom chatRoom = chatRoomRepository.findByApplymentId(applymentId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        return chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoom.getChatRoomId())
                .stream()
                .map(msg -> new ChatMessageResponseDto(
                        "MESSAGE",
                        applymentId,
                        null,
                        msg.getNickname(),
                        msg.getContent(),
                        msg.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")) //브로드캐스트 시 createdAt과 sentAt이 다른 시간이 되면 UX 혼란생길 수 있음
                ))
                .toList();
    }
}
