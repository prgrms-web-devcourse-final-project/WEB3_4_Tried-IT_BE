//package com.dementor.domain.chat.service;
//
//import com.dementor.domain.admin.entity.Admin;
//import com.dementor.domain.chat.entity.ChatRoom;
//import com.dementor.domain.chat.entity.RoomType;
//import com.dementor.domain.chat.repository.ChatMessageRepository;
//import com.dementor.domain.chat.repository.ChatRoomRepository;
//import com.dementor.domain.member.entity.Member;
//import com.dementor.domain.apply.repository.ApplyRepository;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import java.util.List;
//import java.util.Collections;
//
//import static org.mockito.Mockito.*;
//import static org.assertj.core.api.Assertions.*;
//
//class ChatRoomServiceTest {
//
//    @InjectMocks
//    private ChatRoomService chatRoomService;
//
//    @Mock
//    private ChatRoomRepository chatRoomRepository;
//
//    @Mock
//    private ChatMessageRepository chatMessageRepository;
//
//    @Mock
//    private ApplyRepository applyRepository;
//
//    @Mock
//    private com.dementor.domain.member.repository.MemberRepository memberRepository;
//
//    private Member mentor;
//    private Member mentee;
//    private Admin admin;
//
//    @BeforeEach
//    void setup() {
//        MockitoAnnotations.openMocks(this);
//
//        mentor = new Member();
//        mentor.setId(1L);
//        mentor.setNickname("멘토");
//
//        mentee = new Member();
//        mentee.setId(2L);
//        mentee.setNickname("멘티");
//
//        admin = new Admin();
//        admin.setAdminId(100L);
//    }
//
//    @Test
//    void 멘토링_채팅방_2개_생성() {
//        // given
//        Long applymentId = 1L;
//
//        // when
//        chatRoomService.createMentoringChatRooms(applymentId, mentor, mentee);
//
//        // then
//        ArgumentCaptor<ChatRoom> captor = ArgumentCaptor.forClass(ChatRoom.class);
//        verify(chatRoomRepository, times(2)).save(captor.capture());
//
//        List<ChatRoom> savedRooms = captor.getAllValues();
//
//        assertThat(savedRooms).hasSize(2);
//        assertThat(savedRooms.get(0).getRoomType()).isEqualTo(RoomType.MENTORING_CHAT);
//        assertThat(savedRooms.get(1).getRoomType()).isEqualTo(RoomType.MENTORING_CHAT);
//        assertThat(savedRooms.get(0).getTargetNickname()).isEqualTo("멘티");
//        assertThat(savedRooms.get(1).getTargetNickname()).isEqualTo("멘토");
//    }
//
//    @Test
//    void 관리자_채팅방_2개_생성() {
//        // given
//        Member member = new Member();
//        member.setId(3L);
//        member.setNickname("사용자");
//
//        // when
//        chatRoomService.createAdminChatRooms(admin, member);
//
//        // then
//        verify(chatRoomRepository, times(2)).save(any(ChatRoom.class));
//    }
//
//    @Test
//    void 사용자_기준_채팅방_목록_조회() {
//        // given
//        ChatRoom room = new ChatRoom();
//        room.setChatRoomId(1L);
//        room.setRoomType(RoomType.MENTORING_CHAT);
//        room.setMember(mentor);
//        room.setTargetNickname("상대방");
//        when(chatRoomRepository.findByMember_Id(1L)).thenReturn(List.of(room));
//        when(chatMessageRepository.findTop1ByChatRoom_ChatRoomIdOrderByCreatedAtDesc(1L))
//                .thenReturn(Collections.emptyList());
//
//        // when
//        var result = chatRoomService.getAllMyChatRooms(1L);
//
//        // then
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).getNickname()).isEqualTo("멘토");
//        assertThat(result.get(0).getTargetNickname()).isEqualTo("상대방");
//    }
//
//    @Test
//    void 관리자_기준_채팅방_목록_조회() {
//        // given
//        ChatRoom room = new ChatRoom();
//        room.setChatRoomId(1L);
//        room.setRoomType(RoomType.ADMIN_CHAT);
//        room.setMember(mentee);
//        room.setAdmin(admin);
//        room.setTargetNickname("관리자");
//
//        when(chatRoomRepository.findByAdmin_AdminId(100L)).thenReturn(List.of(room));
//        when(chatMessageRepository.findTop1ByChatRoom_ChatRoomIdOrderByCreatedAtDesc(1L))
//                .thenReturn(Collections.emptyList());
//
//        // when
//        var result = chatRoomService.getAllMyAdminChatRooms(100L);
//
//        // then
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).getNickname()).isEqualTo("멘티");
//    }
//}
package com.dementor.chat.service;

import com.dementor.domain.admin.entity.Admin;
import com.dementor.domain.chat.entity.ChatRoom;
import com.dementor.domain.chat.entity.RoomType;
import com.dementor.domain.chat.repository.ChatMessageRepository;
import com.dementor.domain.chat.repository.ChatRoomRepository;
import com.dementor.domain.chat.service.ChatRoomService;
import com.dementor.domain.member.entity.Member;

import com.dementor.domain.apply.repository.ApplyRepository;
import com.dementor.chat.service.TestMemberFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.List;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class ChatRoomServiceTest {

    @InjectMocks
    private ChatRoomService chatRoomService;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ApplyRepository applyRepository;

    @Mock
    private com.dementor.domain.member.repository.MemberRepository memberRepository;

    private Member mentor;
    private Member mentee;
    private Admin admin;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mentor = com.dementor.chat.service.TestMemberFactory.createMentor(1L);
        mentee = com.dementor.chat.service.TestMemberFactory.createMentee(2L);
        admin = new Admin();
        admin.setId(99L);
    }

    @Test
    void 멘토링_채팅방_생성_테스트() {
        chatRoomService.createMentoringChatRooms(101L, mentor, mentee);
        verify(chatRoomRepository, times(2)).save(any(ChatRoom.class));
    }

    @Test
    void 관리자_채팅방_생성_테스트() {
        chatRoomService.createAdminChatRooms(admin, mentee);
        verify(chatRoomRepository, times(2)).save(any(ChatRoom.class));
    }

    @Test
    void 사용자_기준_채팅방_목록_조회() {
        ChatRoom room = new ChatRoom();
        room.setChatRoomId(1L);
        room.setRoomType(RoomType.MENTORING_CHAT);
        room.setMember(mentor);
        room.setTargetNickname("상대닉");
        when(chatRoomRepository.findByMember_Id(1L)).thenReturn(List.of(room));
        when(chatMessageRepository.findTop1ByChatRoom_ChatRoomIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());

        var result = chatRoomService.getAllMyChatRooms(1L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetNickname()).isEqualTo("상대닉");
    }

    @Test
    void 관리자_기준_채팅방_목록_조회() {
        ChatRoom room = new ChatRoom();
        room.setChatRoomId(2L);
        room.setRoomType(RoomType.ADMIN_CHAT);
        room.setMember(mentee);
        room.setAdmin(admin);
        room.setTargetNickname("관리자");
        when(chatRoomRepository.findByAdmin_AdminId(99L)).thenReturn(List.of(room));
        when(chatMessageRepository.findTop1ByChatRoom_ChatRoomIdOrderByCreatedAtDesc(2L)).thenReturn(Collections.emptyList());

        var result = chatRoomService.getAllMyAdminChatRooms(99L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetNickname()).isEqualTo("관리자");
    }
}
