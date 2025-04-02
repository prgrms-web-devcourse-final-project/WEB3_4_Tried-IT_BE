//package com.dementor.chat.service;
//
//import com.dementor.domain.chat.dto.ChatRoomResponseDto;
//import com.dementor.domain.chat.entity.ChatRoom;
//import com.dementor.domain.chat.entity.RoomType;
//import com.dementor.domain.chat.repository.ChatRoomRepository;
//import com.dementor.domain.chat.service.ChatRoomService;
//
//
//
////멤버 도메인
//import com.dementor.domain.member.entity.Member;
//import com.dementor.domain.member.entity.UserRole;
//import com.dementor.domain.member.repository.MemberRepository;
//
////멘토 도메인
//import com.dementor.domain.mentor.entity.Mentor;
//import com.dementor.domain.mentor.repository.MentorRepository;
//
//
////맨토링 클래스 도메인
//import com.dementor.domain.mentoringclass.entity.MentoringClass;
//import com.dementor.domain.mentoringclass.repository.MentoringClassRepository;
//
////apply 도메인
//import com.dementor.domain.apply.entity.Apply;
//import com.dementor.domain.apply.repository.ApplyRepository;
//
//
//import jakarta.transaction.Transactional;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.util.List;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//
//@SpringBootTest
//@ActiveProfiles("test")
//@Transactional // ✅ 영속성 유지
//
//class ChatRoomServiceTest {
//
//    @Autowired ChatRoomService chatRoomService;
//    @Autowired MemberRepository memberRepository;
//    @Autowired
//    MentorRepository mentorRepository;
//    @Autowired ApplyRepository applyRepository;
//    @Autowired MentoringClassRepository mentoringClassRepository;
//    @Autowired ChatRoomRepository chatRoomRepository;
//
//    Member mentee;
//    Mentor mentor;
//    Apply apply;
//
//    @BeforeEach
//    void setup() {
//        String uuid = UUID.randomUUID().toString();
//
//        // ✅ 멘토/멘티 Member 저장
//        Member mentorMember = memberRepository.save(Member.builder()
//                .email("mentor_" + uuid + "@test.com")
//                .nickname("mentor_" + uuid)
//                .name("멘토")
//                .password("1234")
//                .userRole(UserRole.MENTOR)
//                .build());
//
//        mentee = memberRepository.save(Member.builder()
//                .email("mentee_" + uuid + "@test.com")
//                .nickname("mentee_" + uuid)
//                .name("멘티")
//                .password("1234")
//                .userRole(UserRole.MENTEE)
//                .build());
//
//        // ✅ Mentor 엔티티 저장 (필수 필드 임시 값 포함)
//        mentor = mentorRepository.save(Mentor.builder()
//                .member(mentorMember)
//                .job(null) // 필요시 임시 Job 객체 생성 후 대체
//                .name("멘토")
//                .career(3)
//                .phone("010-1234-5678")
//                .introduction("자바 전문 멘토입니다.")
//                .isApproved(ApprovalStatus.Y)
//                .isModified(ApprovalStatus.N)
//                .build());
//
//        // ✅ 멘토링 클래스 생성
//        MentoringClass mentoringClass = mentoringClassRepository.save(MentoringClass.builder()
//                .mentor(mentor)
//                .title("자바 멘토링")
//                .build());
//
//        // ✅ Apply 생성
//        apply = applyRepository.save(Apply.builder()
//                .member(mentee)
//                .mentoringClass(mentoringClass)
//                .build());
//    }
//
//    @AfterEach
//    void tearDown() {
//        chatRoomRepository.deleteAll();
//        applyRepository.deleteAll();
//        mentoringClassRepository.deleteAll();
//        mentorRepository.deleteAll();
//        memberRepository.deleteAll();
//    }
//
//    @Test
//    void 채팅방_생성_및_목록조회_성공() {
//        Long roomId = chatRoomService.createChatRoom(apply.getId(), mentee.getId());
//        assertNotNull(roomId);
//
//        List<ChatRoomResponseDto> chatRooms = chatRoomService.getMyChatRooms(mentor.getMember().getId());
//        assertEquals(1, chatRooms.size());
//
//        ChatRoomResponseDto room = chatRooms.get(0);
//        assertEquals(RoomType.MENTORING_CHAT, room.getRoomType());
//        assertEquals(mentee.getNickname(), room.getNickname());
//    }
//}

package com.dementor.chat.service;

import com.dementor.domain.chat.dto.ChatRoomResponseDto;
import com.dementor.domain.chat.entity.RoomType;
import com.dementor.domain.chat.repository.ChatRoomRepository;
import com.dementor.domain.chat.service.ChatRoomService;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.entity.UserRole;
import com.dementor.domain.member.repository.MemberRepository;
import com.dementor.domain.mentoringclass.entity.MentoringClass;
import com.dementor.domain.mentoringclass.repository.MentoringClassRepository;
import com.dementor.domain.apply.entity.Apply;
import com.dementor.domain.apply.repository.ApplyRepository;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChatRoomServiceTest {

    @Autowired ChatRoomService chatRoomService;
    @Autowired MemberRepository memberRepository;
    @Autowired ApplyRepository applyRepository;
    @Autowired MentoringClassRepository mentoringClassRepository;
    @Autowired ChatRoomRepository chatRoomRepository;

    Member mentee;
    Member mentorMember;
    Apply apply;

    @BeforeEach
    void setup() {
        String uuid = UUID.randomUUID().toString();

        // ✅ 멘토/멘티 Member 저장
        mentorMember = memberRepository.save(Member.builder()
                .email("mentor_" + uuid + "@test.com")
                .nickname("mentor_" + uuid)
                .name("멘토")
                .password("1234")
                .userRole(UserRole.MENTOR)
                .build());

        mentee = memberRepository.save(Member.builder()
                .email("mentee_" + uuid + "@test.com")
                .nickname("mentee_" + uuid)
                .name("멘티")
                .password("1234")
                .userRole(UserRole.MENTEE)
                .build());

        // ✅ MentoringClass는 최소 구성으로 저장 (Member 참조)
        MentoringClass mentoringClass = mentoringClassRepository.save(MentoringClass.builder()
                .member(mentorMember)
                .title("자바 멘토링")
                .build());

        // ✅ Apply 저장
        apply = applyRepository.save(Apply.builder()
                .member(mentee)
                .mentoringClass(mentoringClass)
                .build());
    }

    @AfterEach
    void tearDown() {
        chatRoomRepository.deleteAll();
        applyRepository.deleteAll();
        mentoringClassRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    void 상대방기준_채팅방_목록조회_성공() {
        // ✅ 채팅방 생성 (멘티가 생성 → 상대방은 멘토가 됨)
        Long roomId = chatRoomService.createChatRoom(apply.getId(), mentee.getId());
        assertNotNull(roomId);

        // ✅ 상대방(멘토)이 자신의 채팅 목록을 조회 (getMyChatRooms는 상대방 기준으로 조회됨)
        List<ChatRoomResponseDto> chatRooms = chatRoomService.getMyChatRooms(mentorMember.getId());
        assertEquals(1, chatRooms.size());

        ChatRoomResponseDto room = chatRooms.get(0);
        assertEquals(RoomType.MENTORING_CHAT, room.getRoomType());
        assertEquals(mentee.getNickname(), room.getNickname()); // 상대방 닉네임 = 멘티 닉네임
    }
}
