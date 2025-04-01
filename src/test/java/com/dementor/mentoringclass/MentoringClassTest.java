package com.dementor.mentoringclass;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.dementor.config.TestSecurityConfig;
import com.dementor.domain.job.entity.Job;
import com.dementor.domain.job.repository.JobRepository;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.entity.UserRole;
import com.dementor.domain.member.repository.MemberRepository;
import com.dementor.domain.mentor.entity.Mentor;
import com.dementor.domain.mentor.repository.MentorRepository;
import com.dementor.domain.mentoringclass.dto.request.MentoringClassCreateRequest;
import com.dementor.domain.mentoringclass.dto.request.ScheduleRequest;
import com.dementor.domain.mentoringclass.entity.MentoringClass;
import com.dementor.domain.mentoringclass.entity.Schedule;
import com.dementor.domain.mentoringclass.repository.MentoringClassRepository;
import com.dementor.domain.mentoringclass.repository.ScheduleRepository;
import com.dementor.global.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
public class MentoringClassTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    private MentoringClassRepository mentoringClassRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    private Long testClassId;
    private Long testJobId;
    private Long testMentorId;
    private CustomUserDetails mentorPrincipal;

    @BeforeEach
    void setUp() {
        // 테스트용 멘토 생성
        Member mentor = Member.builder()
                .email("test@test.com")
                .password("password")
                .nickname("테스트 멘토")
                .name("테스트 멘토")
                .userRole(UserRole.MENTOR)
                .build();
        mentor = memberRepository.save(mentor);
        testMentorId = mentor.getId();
        mentorPrincipal = CustomUserDetails.of(mentor);

        // Job 생성
        Job job = Job.builder()
                .name("백엔드 개발자")
                .build();
        job = jobRepository.save(job);
        testJobId = job.getId();

        // Mentor 생성
        Mentor mentorEntity = Mentor.builder()
                .member(mentor)
                .name("테스트 멘토")
                .currentCompany("테스트 회사")
                .career(5)
                .phone("010-1234-5678")
                .introduction("테스트 멘토 소개")
                .isApproved(Mentor.ApprovalStatus.Y)
                .isModified(Mentor.ApprovalStatus.N)
                .job(job)
                .build();
        mentorEntity = mentorRepository.save(mentorEntity);

        // MentoringClass 생성
        MentoringClass mentoringClass = MentoringClass.builder()
                .title("테스트 수업")
                .stack("Spring Boot")
                .content("테스트용 수업 내용")
                .price(50000)
                .mentor(mentorEntity)
                .schedules(new ArrayList<>())  // schedules 리스트 초기화
                .build();
        mentoringClass = mentoringClassRepository.save(mentoringClass);
        testClassId = mentoringClass.getId();

        // Schedule 생성
        Schedule schedule = Schedule.builder()
                .dayOfWeek("월요일")
                .time(10001100)
                .mentoringClass(mentoringClass)
                .build();
        schedule = scheduleRepository.save(schedule);

        // 양방향 연관관계 설정
        mentoringClass.getSchedules().add(schedule);
        mentoringClass = mentoringClassRepository.save(mentoringClass);

        // Security Context에 인증 정보 설정
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                mentorPrincipal,
                null,
                mentorPrincipal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void findAllMentoringClass() throws Exception {
        // given
        // when & then
        mockMvc.perform(get("/api/class")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("멘토링 수업 조회 성공"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void findOneMentoringClass() throws Exception {
        //when
        mockMvc.perform(get("/api/class/{class_id}", testClassId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("멘토링 수업 상세 조회 성공"))
                .andExpect(jsonPath("$.data.classId").value(testClassId))
                .andExpect(jsonPath("$.data.title").value("테스트 수업"))
                .andExpect(jsonPath("$.data.stack").value("Spring Boot"))
                .andExpect(jsonPath("$.data.content").value("테스트용 수업 내용"))
                .andExpect(jsonPath("$.data.price").value(50000));
        //then
    }

    @Test
    void createMentoringClass() throws Exception {
        // given
        MentoringClassCreateRequest request = new MentoringClassCreateRequest(
            "Spring Boot",
            "스프링 부트 기초부터 실전까지",
            "스프링 부트 완전 정복",
            50000,
            List.of(
                new ScheduleRequest("월요일", 10001100)
            )
        );

        // when & then
        mockMvc.perform(post("/api/class")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("멘토링 클래스 생성 성공"))
                .andExpect(jsonPath("$.data").isNumber());
    }
}
