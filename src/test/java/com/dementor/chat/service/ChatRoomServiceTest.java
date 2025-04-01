package com.dementor.chat.service;

import com.dementor.domain.chat.dto.ChatRoomResponseDto;
import com.dementor.domain.chat.entity.ChatRoom;
import com.dementor.domain.chat.entity.RoomType;
import com.dementor.domain.chat.repository.ChatRoomRepository;
import com.dementor.domain.chat.service.ChatRoomService;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.entity.UserRole;
import com.dementor.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ChatRoomServiceTest {

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member savedMember;

    @BeforeEach
    void setup() {
        savedMember = memberRepository.save(Member.builder()
                .email("roomtester@example.com")
                .password("test")
                .nickname("채팅방유저")
                .name("이순신")
                .userRole(UserRole.MENTEE)
                .build());

        ChatRoom room = new ChatRoom();
        room.setApplymentId(888L);
        room.setMember(savedMember);
        room.setRoomType(RoomType.MENTORING_CHAT);
        chatRoomRepository.save(room);
    }

    @Test
    void 채팅방_목록_조회_성공() {
        List<ChatRoomResponseDto> rooms = chatRoomService.getMyChatRooms(savedMember.getId());
        assertNotNull(rooms);
        assertEquals(1, rooms.size());
        assertEquals("채팅방유저", rooms.get(0).getNickname());
    }
}
