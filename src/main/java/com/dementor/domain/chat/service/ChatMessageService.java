package com.dementor.domain.chat.service;

import com.dementor.domain.chat.dto.ChatMessageResponseDto;
import com.dementor.domain.chat.dto.ChatMessageSendDto;
import com.dementor.domain.chat.entity.ChatMessage;
import com.dementor.domain.chat.entity.ChatRoom;
import com.dementor.domain.chat.entity.MessageType;
import com.dementor.domain.chat.repository.ChatMessageRepository;
import com.dementor.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private static final TimeZone KST = TimeZone.getTimeZone("Asia/Seoul");

    // 1. 메시지 저장 - REST 방식 사용 가능
    @Transactional
    public ChatMessageResponseDto sendMessage(ChatMessageSendDto dto) {
        ChatRoom chatRoom = chatRoomRepository.findById(dto.getChatRoomId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderId(dto.getSenderId())
                .senderType(dto.getSenderType())
                .content(dto.getContent())
//                .messageType(MessageType.TALK)
                .sentAt(LocalDateTime.now())
                .build();

        chatMessageRepository.save(chatMessage);
        chatRoom.updateLastMessageTime(chatMessage.getSentAt());

        ChatMessageResponseDto responseDto = new ChatMessageResponseDto(
//                chatMessage.getMessageType(),
                chatMessage.getChatRoom().getChatRoomId(),
                chatMessage.getSenderId(),
                chatMessage.getSenderType(),
//                null, // nickname은 추후 처리
                chatMessage.getContent(),
                chatMessage.getSentAt().atZone(KST.toZoneId())
        );

        // 2. 실시간 브로드캐스트
        messagingTemplate.convertAndSend("/sub/chat/room/" + chatRoom.getChatRoomId(), responseDto);

        return responseDto;
    }

    // 3. 메시지 목록 조회
    @Transactional(readOnly = true)
    public List<ChatMessageResponseDto> getMessages(Long chatRoomId, Long beforeMessageId) {
        List<ChatMessage> messages;

        if (beforeMessageId != null && beforeMessageId > 0) {
            // 이전 메시지부터 20개 가져오기 (과거로 스크롤)
            messages = chatMessageRepository
                    .findTop20ByChatRoom_ChatRoomIdAndChatMessageIdLessThanOrderByChatMessageIdDesc(chatRoomId, beforeMessageId);
        } else {
            // 최초 입장 시 최신 메시지부터
            messages = chatMessageRepository
                    .findTop20ByChatRoom_ChatRoomIdOrderByChatMessageIdDesc(chatRoomId);
        }

        // 프론트에선 오래된 → 최신 순으로 보여줘야 하므로 역순 정렬
        List<ChatMessage> sorted = messages.stream()
                .sorted((m1, m2) -> Long.compare(m1.getChatMessageId(), m2.getChatMessageId()))
                .toList();

        return sorted.stream().map(chatMessage -> new ChatMessageResponseDto(
                chatMessage.getChatRoom().getChatRoomId(),
                chatMessage.getSenderId(),
                chatMessage.getSenderType(),
                chatMessage.getContent(),
                chatMessage.getSentAt().atZone(KST.toZoneId())
        )).collect(Collectors.toList());
    }



    // 4. 웹소켓 메시지 수신 처리 (컨트롤러 대신 서비스에서 분리 가능)
    @MessageMapping("/chat/rooms/{chatRoomId}/messages/create")
    public void receiveMessageWebsocket(@DestinationVariable Long chatRoomId, ChatMessageSendDto dto) {
        dto.setChatRoomId(chatRoomId);
        sendMessage(dto); // 저장 및 브로드캐스트 호출
    }
}
