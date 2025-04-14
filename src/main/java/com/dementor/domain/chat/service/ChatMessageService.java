package com.dementor.domain.chat.service;

import com.dementor.domain.chat.dto.ChatMessageResponseDto;
import com.dementor.domain.chat.dto.ChatMessageSendDto;
import com.dementor.domain.chat.entity.ChatMessage;
import com.dementor.domain.chat.entity.ChatRoom;
import com.dementor.domain.chat.entity.MessageType;
import com.dementor.domain.chat.entity.SenderType;
import com.dementor.domain.chat.repository.ChatMessageRepository;
import com.dementor.domain.chat.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

//import com.dementor.global.websocket.StompRabbitMqBrokerConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.simp.SimpMessagingTemplate;

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
	private final RabbitTemplate rabbitTemplate;
	private final ChatRoomService chatRoomService;

	private static final TimeZone KST = TimeZone.getTimeZone("Asia/Seoul");

	/**
	 *  1. 메시지 목록 조회
	 * - 사용자가 채팅방에 입장할 때 과거 메시지 불러오기
	 */
	@Transactional(readOnly = true)
	public List<ChatMessageResponseDto> getMessages(Long chatRoomId, Long beforeMessageId) {
		List<ChatMessage> messages;

		if (beforeMessageId != null && beforeMessageId > 0) {
			// 과거 스크롤 시: 이전 메시지부터 20개
			messages = chatMessageRepository
				.findTop20ByChatRoom_ChatRoomIdAndChatMessageIdLessThanOrderByChatMessageIdDesc(chatRoomId,
					beforeMessageId);
		} else {
			// 첫 입장 시: 최신 메시지부터 20개
			messages = chatMessageRepository
				.findTop20ByChatRoom_ChatRoomIdOrderByChatMessageIdDesc(chatRoomId);
		}

		// 오래된 순으로 정렬
		return messages.stream()
			.sorted((m1, m2) -> Long.compare(m1.getChatMessageId(), m2.getChatMessageId()))
			.map(chatMessage -> new ChatMessageResponseDto(
				chatMessage.getChatRoom().getChatRoomId(),
				chatMessage.getSenderId(),
				chatMessage.getSenderType(),
				chatMessage.getContent(),
				chatMessage.getSentAt().atZone(KST.toZoneId())
			)).toList();
	}

	/**
	 * 2. 메시지 저장 및 실시간 전송
	 * - 사용자가 메시지를 보낼 때 호출
	 * - DB에 저장 후, RabbitMQ 통해 실시간 브로드캐스트
	 */
	@Transactional
//	public ChatMessageResponseDto sendMessage(Long chatRoomId, ChatMessageSendDto dto, Long senderId, SenderType senderType) {
    public ChatMessageResponseDto sendMessage(Long chatRoomId, ChatMessageSendDto dto) {

//
//		// 참여자 검증 로직 추가 (기존 ChatRoomService 로직 재사용)
//		chatRoomService.getChatRoomDetail(
//				dto.getChatRoomId(),
//				dto.getSenderId(),          // senderId == viewerId
//				dto.getSenderType().name().toLowerCase() // senderType == viewerType ( enum → "member"/"admin")
//		);

		// 채팅방 유효성 검사
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        // 메시지 엔티티 생성
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderId(dto.getSenderId())
                .senderType(dto.getSenderType())
                .content(dto.getContent())
                .sentAt(LocalDateTime.now())
                .build();

        // DB 저장
		chatMessageRepository.save(chatMessage);
		chatRoom.updateLastMessageTime(chatMessage.getSentAt());

		// 응답 DTO 생성
		ChatMessageResponseDto responseDto = new ChatMessageResponseDto(
			chatMessage.getChatRoom().getChatRoomId(),
			chatMessage.getSenderId(),
			chatMessage.getSenderType(),
			chatMessage.getContent(),
			chatMessage.getSentAt().atZone(KST.toZoneId())
		);

		// RabbitMQ로 브로드캐스트 전송
		rabbitTemplate.convertAndSend(
			"amq.topic",
			"chat.room." + chatRoom.getChatRoomId(),
			responseDto
		);

		return responseDto;
	}
}

