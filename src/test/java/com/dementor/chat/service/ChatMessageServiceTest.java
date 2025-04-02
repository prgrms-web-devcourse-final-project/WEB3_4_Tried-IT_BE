package com.dementor.chat.service;

import com.dementor.domain.chat.dto.ChatMessageSendDto;
import com.dementor.domain.chat.dto.ChatMessageSliceDto;
import com.dementor.domain.chat.entity.ChatMessage;
import com.dementor.domain.chat.entity.ChatRoom;
import com.dementor.domain.chat.entity.MessageType;
import com.dementor.domain.chat.entity.RoomType;
import com.dementor.domain.chat.repository.ChatMessageRepository;
import com.dementor.domain.chat.repository.ChatRoomRepository;
import com.dementor.domain.chat.service.ChatMessageService;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.entity.UserRole;
import com.dementor.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest
class ChatMessageServiceTest {

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ChatMessageService chatMessageService;

    Member sender;
    ChatRoom mentoringRoom;
    ChatRoom adminRoom;

    @BeforeEach
    void setUp() {
        // 고유 이메일과 닉네임 생성
        String uuid = UUID.randomUUID().toString();

        sender = Member.builder()
                .email("sender_" + uuid + "@example.com")
                .password("encodedPassword")
                .nickname("닉네임_" + uuid)
                .name("테스트유저")
                .userRole(UserRole.MENTEE)
                .build();
        sender = memberRepository.save(sender);

        // 멘토링 채팅방
        mentoringRoom = new ChatRoom();
        mentoringRoom.setApplymentId(200L);
        mentoringRoom.setRoomType(RoomType.MENTORING_CHAT);
        mentoringRoom.setMember(sender);
        mentoringRoom = chatRoomRepository.save(mentoringRoom);

        // 관리자 채팅방
        adminRoom = new ChatRoom();
        adminRoom.setRoomType(RoomType.ADMIN_CHAT);
        adminRoom.setMember(sender); // 실제론 관리자와 연결되지만 테스트에선 sender로 사용
        adminRoom = chatRoomRepository.save(adminRoom);
    }

    @AfterEach
    void tearDown() {
        chatMessageRepository.deleteAll();
        chatRoomRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    void 멘토링_메시지_저장_조회_성공() {
        // given
        ChatMessageSendDto dto = new ChatMessageSendDto(
                MessageType.MESSAGE,
                mentoringRoom.getApplymentId(),
                "안녕하세요 멘토님"
        );

        // when
        chatMessageService.handleMessage(dto, sender.getId(), sender.getNickname());

        // then
        ChatMessageSliceDto result = chatMessageService.getMessages(
                mentoringRoom.getChatRoomId(), null, 20
        );

        assertEquals(1, result.getMessages().size());
        assertEquals("안녕하세요 멘토님", result.getMessages().get(0).getMessage());
        assertEquals(sender.getNickname(), result.getMessages().get(0).getNickname());
    }

    @Test
    void 관리자_메시지_저장_조회_성공() {
        // given
        ChatMessageSendDto dto = new ChatMessageSendDto(
                MessageType.MESSAGE,
                null,
                "관리자님 궁금한 게 있어요!"
        );

        // when
        chatMessageService.handleMessage(dto, sender.getId(), sender.getNickname());

        // then
        ChatMessageSliceDto result = chatMessageService.getMessages(
                adminRoom.getChatRoomId(), null, 20
        );

        assertEquals(1, result.getMessages().size());
        assertEquals("관리자님 궁금한 게 있어요!", result.getMessages().get(0).getMessage());
        assertEquals(sender.getNickname(), result.getMessages().get(0).getNickname());
    }
}
