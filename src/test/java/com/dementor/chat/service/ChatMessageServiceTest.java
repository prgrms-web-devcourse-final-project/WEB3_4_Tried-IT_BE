package com.dementor.chat.service;

import com.dementor.config.TestSecurityConfig;
import com.dementor.domain.apply.dto.request.ApplyRequest;
import com.dementor.domain.apply.service.ApplyService;
import com.dementor.domain.chat.dto.ChatMessageResponseDto;
import com.dementor.domain.chat.dto.ChatMessageSendDto;
import com.dementor.domain.chat.entity.MessageType;
import com.dementor.domain.chat.entity.SenderType;
import com.dementor.domain.chat.repository.ChatRoomRepository;
import com.dementor.domain.chat.service.ChatMessageService;
import com.dementor.domain.job.entity.Job;
import com.dementor.domain.job.repository.JobRepository;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.entity.UserRole;
import com.dementor.domain.member.repository.MemberRepository;
import com.dementor.domain.mentor.entity.Mentor;
import com.dementor.domain.mentor.repository.MentorRepository;
import com.dementor.domain.mentoringclass.entity.MentoringClass;
import com.dementor.domain.mentoringclass.repository.MentoringClassRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class ChatMessageServiceTest {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ApplyService applyService;

    @Autowired
    private MentoringClassRepository mentoringClassRepository;

    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    private JobRepository jobRepository;

    private Member testMentor;
    private Member testMentee;
    private Long chatRoomId;

    @BeforeEach
    void setUp() {
        // 멘토
        testMentor = Member.builder()
                .email("mentor@test.com")
                .password("password")
                .nickname("testMentor")
                .name("테스트멘토")
                .userRole(UserRole.MENTOR)
                .build();
        testMentor = memberRepository.save(testMentor);

        // 멘티
        testMentee = Member.builder()
                .email("mentee@test.com")
                .password("password")
                .nickname("testMentee")
                .name("테스트멘티")
                .userRole(UserRole.MENTEE)
                .build();
        testMentee = memberRepository.save(testMentee);

        // 직업 & 멘토 & 클래스 생성
        Job job = jobRepository.save(Job.builder().name("백엔드").build());

        Mentor mentor = mentorRepository.save(Mentor.builder()
                .member(testMentor)
                .name("테스트멘토")
                .job(job)
                .career(3)
                .phone("010-0000-0000")
                .introduction("소개")
                .build());

        MentoringClass mentoringClass = mentoringClassRepository.save(
                MentoringClass.builder()
                        .mentor(mentor)
                        .title("테스트 클래스")
                        .content("설명")
                        .price(10000)
                        .stack("Java")
                        .build()
        );

        // 멘토링 신청 → 내부적으로 채팅방 생성됨
        ApplyRequest.ApplyCreateRequest request = new ApplyRequest.ApplyCreateRequest();
        request.setClassId(mentoringClass.getId());
        request.setInquiry("신청합니다");
        request.setSchedule(LocalDateTime.now().plusDays(1));

        ApplyResponse.GetApplyId response = applyService.createApply(request, testMentee.getId());
        this.chatRoomId = response.getChatRoomId();
    }

    @Test
    @DisplayName("멘토링 채팅방에 메시지 전송 성공")
    void sendMessageToMentoringChatRoom() {
        // given
        ChatMessageSendDto dto = new ChatMessageSendDto();
        dto.setChatRoomId(chatRoomId);
        dto.setType(MessageType.MESSAGE);
        dto.setMessage("안녕하세요, 테스트 메시지입니다.");

        // when
        ChatMessageResponseDto result = chatMessageService.handleMessage(dto, testMentor.getId(), SenderType.MEMBER);

        // then
        assertThat(result.getChatRoomId()).isEqualTo(chatRoomId);
        assertThat(result.getSenderId()).isEqualTo(testMentor.getId());
        assertThat(result.getNickname()).isEqualTo("testMentee");
        assertThat(result.getMessage()).isEqualTo("안녕하세요, 테스트 메시지입니다.");
        assertThat(result.getSentAt()).isNotNull();
    }
}
