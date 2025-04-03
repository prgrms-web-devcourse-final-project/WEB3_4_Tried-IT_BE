package com.dementor.chat.service;


import com.dementor.domain.admin.repository.AdminRepository;
import com.dementor.domain.chat.dto.ChatMessageResponseDto;
import com.dementor.domain.chat.dto.ChatMessageSendDto;
import com.dementor.domain.chat.dto.ChatMessageSliceDto;
import com.dementor.domain.chat.entity.ChatMessage;
import com.dementor.domain.chat.entity.ChatRoom;
import com.dementor.domain.chat.entity.MessageType;
import com.dementor.domain.chat.entity.SenderType;
import com.dementor.domain.chat.repository.ChatMessageRepository;
import com.dementor.domain.chat.repository.ChatRoomRepository;
import com.dementor.domain.chat.service.ChatMessageService;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ChatMessageServiceTest {

    private ChatMessageRepository chatMessageRepository;
    private ChatRoomRepository chatRoomRepository;
    private MemberRepository memberRepository;
    private AdminRepository adminRepository;

    private ChatMessageService chatMessageService;

    @BeforeEach
    void setUp() {
        chatMessageRepository = mock(ChatMessageRepository.class);
        chatRoomRepository = mock(ChatRoomRepository.class);
        memberRepository = mock(MemberRepository.class);
        adminRepository = mock(AdminRepository.class);

        chatMessageService = new ChatMessageService(chatMessageRepository, chatRoomRepository, memberRepository, adminRepository);
    }

    @Test
    void 멤버_메시지_저장_테스트() {
        ChatRoom room = ChatRoom.builder().chatRoomId(1L).build();
        Member member = Member.builder().id(100L).nickname("사용자A").build();

        ChatMessageSendDto dto = new ChatMessageSendDto();
        dto.setChatRoomId(1L);
        dto.setType(MessageType.MESSAGE);
        dto.setMessage("안녕하세요!");

        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(memberRepository.findById(100L)).thenReturn(Optional.of(member));

        ChatMessageResponseDto result = chatMessageService.handleMessage(dto, 100L, SenderType.MEMBER);

        assertThat(result.getMessage()).isEqualTo("안녕하세요!");
        assertThat(result.getNickname()).isEqualTo("사용자A");
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
    }

    @Test
    void 관리자_메시지_저장_테스트() {
        ChatRoom room = ChatRoom.builder().chatRoomId(1L).build();

        ChatMessageSendDto dto = new ChatMessageSendDto();
        dto.setChatRoomId(1L);
        dto.setType(MessageType.MESSAGE);
        dto.setMessage("관리자입니다");

        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(room));

        ChatMessageResponseDto result = chatMessageService.handleMessage(dto, 999L, SenderType.ADMIN);

        assertThat(result.getMessage()).isEqualTo("관리자입니다");
        assertThat(result.getNickname()).isEqualTo("관리자");
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
    }

    @Test
    void 메시지_조회_테스트() {
        ChatRoom room = ChatRoom.builder().chatRoomId(1L).build();
        Member member = Member.builder().id(100L).nickname("멘티").build();

        ChatMessage msg = ChatMessage.builder()
                .chatMessageId(10L)
                .chatRoom(room)
                .senderId(100L)
                .senderType(SenderType.MEMBER)
                .messageType(MessageType.MESSAGE)
                .content("테스트 메시지")
                .sentAt(LocalDateTime.now())
                .build();

        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(chatMessageRepository.findTop20ByChatRoom_ChatRoomIdOrderByChatMessageIdDesc(1L)).thenReturn(List.of(msg));
        when(memberRepository.findById(100L)).thenReturn(Optional.of(member));

        ChatMessageSliceDto result = chatMessageService.getMessages(1L, null, 20);

        assertThat(result.getMessages()).hasSize(1);
        assertThat(result.getMessages().get(0).getMessage()).isEqualTo("테스트 메시지");
        assertThat(result.getMessages().get(0).getNickname()).isEqualTo("멘티");
    }
}

