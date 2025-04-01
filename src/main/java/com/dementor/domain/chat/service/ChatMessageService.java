package com.dementor.domain.chat.service;

import com.dementor.domain.chat.dto.ChatMessageResponseDto;
import com.dementor.domain.chat.dto.ChatMessageSendDto;
import com.dementor.domain.chat.dto.ChatMessageSliceDto;
import com.dementor.domain.chat.entity.ChatMessage;
import com.dementor.domain.chat.entity.ChatRoom;
import com.dementor.domain.chat.entity.MessageType;
import com.dementor.domain.chat.repository.ChatMessageRepository;
import com.dementor.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // 메시지 저장
    @Transactional
    public ChatMessageResponseDto handleMessage(ChatMessageSendDto dto, Long memberId, String nickname) {
        ChatRoom chatRoom = chatRoomRepository.findByApplymentId(dto.getApplymentId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다. applymentId=" + dto.getApplymentId()));

        ChatMessage message = new ChatMessage();
        message.setChatRoom(chatRoom);
        message.setType(dto.getType());
        message.setNickname(nickname);
        message.setMemberId(memberId);
        message.setContent(dto.getType() == MessageType.MESSAGE ? dto.getMessage() : null);

        chatMessageRepository.save(message);

        return new ChatMessageResponseDto(
                message.getType(),
                chatRoom.getApplymentId(),
                memberId,
                nickname,
                message.getContent(),
                message.getCreatedAt().atZone(KST)
        );
    }

    // 메시지 페이징 조회 (커서기반)
    @Transactional(readOnly = true)
    public ChatMessageSliceDto getMessages(Long chatRoomId, Long beforeMessageId, int size) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다. chatRoomId=" + chatRoomId));

        List<ChatMessage> messages = (beforeMessageId != null)
                ? chatMessageRepository.findTop20ByChatRoom_ChatRoomIdAndChatMessageIdLessThanOrderByChatMessageIdDesc(chatRoomId, beforeMessageId)
                : chatMessageRepository.findTop20ByChatRoom_ChatRoomIdOrderByChatMessageIdDesc(chatRoomId);

        List<ChatMessageResponseDto> dtoList = messages.stream()
                .map(m -> new ChatMessageResponseDto(
                        m.getType(),
                        chatRoom.getApplymentId(),
                        m.getMemberId(),
                        m.getNickname(),
                        m.getContent(),
                        m.getCreatedAt().atZone(KST)
                ))
                .collect(Collectors.toList());

        Long nextCursor = messages.isEmpty() ? null : messages.get(messages.size() - 1).getChatMessageId();

        return new ChatMessageSliceDto(dtoList, dtoList.size() == size, nextCursor);
    }
}
