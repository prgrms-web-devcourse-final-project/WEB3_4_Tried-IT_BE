package com.dementor.domain.chat.service;

import com.dementor.domain.chat.dto.ChatMessageResponseDto;
import com.dementor.domain.chat.dto.ChatMessageSendDto;
import com.dementor.domain.chat.dto.ChatMessageSliceDto;
import com.dementor.domain.chat.entity.ChatMessage;
import com.dementor.domain.chat.entity.ChatRoom;
import com.dementor.domain.chat.entity.MessageType;
import com.dementor.domain.chat.entity.SenderType;
import com.dementor.domain.chat.repository.ChatMessageRepository;
import com.dementor.domain.chat.repository.ChatRoomRepository;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.repository.MemberRepository;
import com.dementor.domain.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import static io.lettuce.core.GeoArgs.Unit.m;
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final AdminRepository adminRepository;
    private final ChatRoomService chatRoomService;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");


    // 메시지 저장 및 닉네임 분기 처리
    @Transactional
    public ChatMessageResponseDto handleMessage(ChatMessageSendDto dto, Long senderId, SenderType senderType) {
        ChatRoom chatRoom = chatRoomRepository.findById(dto.getChatRoomId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다. chatRoomId=" + dto.getChatRoomId()));

        // 닉네임 분기 처리
        String nickname = senderType == SenderType.SYSTEM
                ? "시스템"
                : chatRoomService.getTargetNickname(chatRoom, senderId);

        log.info("💾 저장할 메시지: chatRoomId={}, senderId={}, nickname={}, content={}",
                chatRoom.getChatRoomId(), senderId, nickname, dto.getMessage());

        // 메시지 생성 및 저장
        ChatMessage message = new ChatMessage();
        message.setChatRoom(chatRoom);
        message.setMessageType(dto.getType());
        message.setSenderId(senderId);
        message.setSenderType(senderType);
        message.setContent(dto.getType() == MessageType.MESSAGE ? dto.getMessage() : null);
        message.setSentAt(LocalDateTime.now()); // sentAt 명시적 설정


        chatMessageRepository.save(message);
        chatRoom.updateLastMessageTime(message.getSentAt()); // 마지막 메시지 시간 갱신


        //  응답 DTO 생성
        return new ChatMessageResponseDto(
                message.getMessageType(),
                chatRoom.getChatRoomId(),
                senderId,
                senderType,
                nickname,
                message.getContent(),
                message.getSentAt().atZone(KST)
        );
    }


     //채팅 메시지 조회 (커서 기반)

    @Transactional(readOnly = true)
    public ChatMessageSliceDto getMessages(Long chatRoomId, Long beforeMessageId, int size) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다. chatRoomId=" + chatRoomId));

        List<ChatMessage> messages = (beforeMessageId != null)
                ? chatMessageRepository.findTop20ByChatRoom_ChatRoomIdAndChatMessageIdLessThanOrderByChatMessageIdDesc(chatRoomId, beforeMessageId)
                : chatMessageRepository.findTop20ByChatRoom_ChatRoomIdOrderByChatMessageIdDesc(chatRoomId);

            // 닉네임 분기, chatRoomService 사용
            List<ChatMessageResponseDto> dtoList = messages.stream().map((ChatMessage m) -> {
                String nickname = m.getSenderType() == SenderType.SYSTEM
                        ? "시스템"
                        : chatRoomService.getTargetNickname(chatRoom, m.getSenderId());

            return new ChatMessageResponseDto(
                    m.getMessageType(),
                    chatRoomId,
                    m.getSenderId(),
                    m.getSenderType(),
                    nickname,
                    m.getContent(),
                    m.getSentAt().atZone(KST)
            );
        }).collect(Collectors.toList());

        Long nextCursor = messages.isEmpty() ? null : messages.get(messages.size() - 1).getChatMessageId();

        return new ChatMessageSliceDto(dtoList, dtoList.size() == size, nextCursor);
    }
}
