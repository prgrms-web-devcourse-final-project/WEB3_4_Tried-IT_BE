package com.dementor.chat.service;

import com.dementor.domain.chat.dto.ChatMessageResponseDto;
import com.dementor.domain.chat.dto.ChatMessageSendDto;
import com.dementor.domain.chat.dto.ChatMessageSliceDto;
import com.dementor.domain.chat.entity.ChatRoom;
import com.dementor.domain.chat.entity.MessageType;
import com.dementor.domain.chat.entity.RoomType;
import com.dementor.domain.chat.repository.ChatMessageRepository;
import com.dementor.domain.chat.repository.ChatRoomRepository;
import com.dementor.domain.chat.service.ChatMessageService;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.entity.UserRole;
import com.dementor.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ChatMessageServiceTest {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member savedMember;
    private ChatRoom savedRoom;

    @BeforeEach
    void setup() {
        savedMember = memberRepository.save(Member.builder()
                .email("tester@example.com")
                .password("test") // 실제 환경에선 인코딩 필요
                .nickname("테스터")
                .name("홍길동")
                .userRole(UserRole.MENTEE)
                .build());

        savedRoom = new ChatRoom();
        savedRoom.setApplymentId(500L);
        savedRoom.setMember(savedMember);
        savedRoom.setRoomType(RoomType.MENTORING_CHAT);
        chatRoomRepository.save(savedRoom);
    }

    @Test
    void 메시지_저장_및_조회_성공() {
        ChatMessageSendDto dto = new ChatMessageSendDto(MessageType.MESSAGE, 500L, "Hello test message");
        ChatMessageResponseDto response = chatMessageService.handleMessage(dto, savedMember.getId(), savedMember.getNickname());

        ChatMessageSliceDto result = chatMessageService.getMessages(savedRoom.getChatRoomId(), null, 20);
        List<ChatMessageResponseDto> messages = result.getMessages();

        assertEquals(1, messages.size());
        assertEquals("Hello test message", messages.get(0).getMessage());
        assertEquals(savedMember.getId(), messages.get(0).getMemberId());
        assertEquals(savedMember.getNickname(), messages.get(0).getNickname());
    }
}
