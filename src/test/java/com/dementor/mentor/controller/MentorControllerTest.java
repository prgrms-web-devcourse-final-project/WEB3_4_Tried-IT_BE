package com.dementor.mentor.controller;

import com.dementor.domain.job.entity.Job;
import com.dementor.domain.job.repository.JobRepository;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.entity.UserRole;
import com.dementor.domain.member.repository.MemberRepository;
import com.dementor.domain.mentor.dto.request.MentorApplicationRequest;
import com.dementor.domain.mentor.dto.request.MentorUpdateRequest;
import com.dementor.domain.mentor.entity.Mentor;
import com.dementor.domain.mentor.entity.MentorModification;
import com.dementor.domain.mentor.repository.MentorApplicationRepository;
import com.dementor.domain.mentor.repository.MentorModificationRepository;
import com.dementor.domain.mentor.repository.MentorRepository;
import com.dementor.global.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class MentorControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private MentorRepository mentorRepository;

    private Member testMember;
    private Member testMentor;
    private Job testJob;
    private CustomUserDetails memberPrincipal;
    private CustomUserDetails mentorPrincipal;
    private Long testMemberId;
    private Long testMentorId;
    private Long testJobId;
    @Autowired
    private MentorModificationRepository mentorModificationRepository;

    @Autowired
    private MentorApplicationRepository mentorApplicationRepository;

    @BeforeEach
    void setUp() {
        // 기존 데이터 정리
        mentorModificationRepository.deleteAll();
        mentorApplicationRepository.deleteAll();
        mentorRepository.deleteAll();
        memberRepository.deleteAll();
        jobRepository.deleteAll();

        // 테스트용 일반 회원 생성
        testMember = Member.builder()
                .nickname("testMember")
                .password("password")
                .name("테스트회원")
                .email("testmember@test.com")
                .userRole(UserRole.MENTEE)
                .build();
        testMember = memberRepository.save(testMember);
        testMemberId = testMember.getId();
        memberPrincipal = CustomUserDetails.of(testMember);

        // 테스트용 멘토 회원 생성
        testMentor = Member.builder()
                .nickname("testMentor")
                .password("password")
                .name("테스트멘토")
                .email("testmentor@test.com")
                .userRole(UserRole.MENTOR)
                .build();
        testMentor = memberRepository.save(testMentor);
        testMentorId = testMentor.getId();
        mentorPrincipal = CustomUserDetails.of(testMentor);

        // 테스트용 직무 생성
        testJob = Job.builder()
                .name("백엔드 개발자")
                .build();
        testJob = jobRepository.save(testJob);
        testJobId = testJob.getId();

        // 테스트용 멘토 정보 생성 (수정/조회 테스트용)
        Mentor mentorInfo = Mentor.builder()
                .member(testMentor)
                .job(testJob)
                .name(testMentor.getName())
                .currentCompany("현재 회사")
                .career(5)
                .phone("01012345678")
                .email("testmentor@test.com")
                .introduction("자기소개")
                .bestFor("특기")
                .approvalStatus(Mentor.ApprovalStatus.APPROVED)
                .modificationStatus(Mentor.ModificationStatus.NONE)
                .mentorings(new ArrayList<>())
                .build();
        mentorRepository.save(mentorInfo);
    }

    @Test
    @DisplayName("멘토 지원 성공")
    @WithMockUser(roles = "MENTEE")
    void applyMentorSuccess() throws Exception {
        // Given
        MentorApplicationRequest.MentorApplicationRequestDto requestDto =
                new MentorApplicationRequest.MentorApplicationRequestDto(
                        testMemberId,
                        testMember.getName(),
                        testJobId,
                        "01012345678",
                        "testmember@test.com",
                        5,
                        "테스트 회사",
                        "테스트 자기소개",
                        "테스트 특기",
                        null
                );

        // When
        ResultActions resultActions = mvc
                .perform(
                        post("/api/mentor")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))
                                .with(user(memberPrincipal))
                )
                .andDo(print());

        // Then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("201"))
                .andExpect(jsonPath("$.message").value("멘토 지원에 성공했습니다."));
    }

    @Test
    @DisplayName("멘토 정보 수정 성공")
    @WithMockUser(roles = "MENTOR")
    void updateMentorSuccess() throws Exception {
        // Given
        MentorUpdateRequest.MentorUpdateRequestDto requestDto =
                new MentorUpdateRequest.MentorUpdateRequestDto(
                        8,          // career
                        "01098765432",    // phone
                        "업데이트 회사",    // currentCompany
                        1L,               // jobId (예시 값)
                        "update@email.com", // email
                        "업데이트된 자기소개", // introduction
                        "업데이트된 특기",   // bestFor
                        null              // attachmentId
                );

        // When
        ResultActions resultActions = mvc
                .perform(
                        put("/api/mentor/" + testMentorId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))
                                .with(user(mentorPrincipal))
                )
                .andDo(print());

        // Then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.message").value("멘토 정보 수정 요청에 성공했습니다."))
                .andExpect(jsonPath("$.data.memberId").value(testMentorId))
                .andExpect(jsonPath("$.data.modificationStatus").value("PENDING"));
    }

    @Test
    @DisplayName("멘토 정보 조회 성공")
    @WithMockUser(roles = "MENTOR")
    void getMentorInfoSuccess() throws Exception {
        // When
        ResultActions resultActions = mvc
                .perform(
                        get("/api/mentor/" + testMentorId + "/info")
                                .with(user(mentorPrincipal))
                )
                .andDo(print());

        // Then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.message").value("멘토 정보 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.memberInfo.memberId").value(testMentorId))
                .andExpect(jsonPath("$.data.memberInfo.name").value("테스트멘토"))
                .andExpect(jsonPath("$.data.memberInfo.job").value("백엔드 개발자"));
    }

    @Test
    @DisplayName("존재하지 않는 멘토 정보 조회시 실패")
    @WithMockUser(roles = "MENTOR")
    void getMentorInfoFail() throws Exception {
        // Given
        Long nonExistentMentorId = 9999L;

        // When
        ResultActions resultActions = mvc
                .perform(
                        get("/api/mentor/" + nonExistentMentorId + "/info")
                                .with(user(mentorPrincipal))
                )
                .andDo(print());

        // Then
        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.message").value("해당 멘토 정보를 수정할 권한이 없습니다."));
    }

    @Test
    @DisplayName("멘토 정보 수정 요청 조회 성공")
    @WithMockUser(roles = "MENTOR")
    void getModificationRequestsSuccess() throws Exception {
        // Given
        // 수정 요청을 생성 (이미 setUp 메서드에서 testMentor가 APPROVED 상태)
        MentorModification modification = MentorModification.builder()
                .member(testMentor)
                .changes("{\"career\":{\"before\":5,\"after\":8},\"phone\":{\"before\":\"01012345678\",\"after\":\"01098765432\"}}")
                .status(MentorModification.ModificationStatus.PENDING)
                .build();
        mentorModificationRepository.save(modification);

        // When
        ResultActions resultActions = mvc
                .perform(
                        get("/api/mentor/" + testMentorId + "/modification-requests")
                                .param("page", "1")
                                .param("size", "10")
                                .with(user(mentorPrincipal))
                )
                .andDo(print());

        // Then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.message").value("멘토 정보 수정 요청 목록 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.modificationRequests").isArray())
                .andExpect(jsonPath("$.data.modificationRequests[0].status").value("PENDING"))
                .andExpect(jsonPath("$.data.pagination.page").value(1))
                .andExpect(jsonPath("$.data.pagination.size").value(10));
    }

    @Test
    @DisplayName("멘토 정보 수정 요청 조회 - 상태별 필터링 성공")
    @WithMockUser(roles = "MENTOR")
    void getModificationRequestsWithStatusFilterSuccess() throws Exception {
        // Given
        // PENDING 상태의 수정 요청 생성
        MentorModification pendingModification = MentorModification.builder()
                .member(testMentor)
                .changes("{\"career\":{\"before\":5,\"after\":8}}")
                .status(MentorModification.ModificationStatus.PENDING)
                .build();
        mentorModificationRepository.save(pendingModification);

        // APPROVED 상태의 수정 요청 생성
        MentorModification approvedModification = MentorModification.builder()
                .member(testMentor)
                .changes("{\"phone\":{\"before\":\"01012345678\",\"after\":\"01098765432\"}}")
                .status(MentorModification.ModificationStatus.APPROVED)
                .build();
        mentorModificationRepository.save(approvedModification);

        // When - APPROVED 상태만 필터링하여 조회
        ResultActions resultActions = mvc
                .perform(
                        get("/api/mentor/" + testMentorId + "/modification-requests")
                                .param("status", "APPROVED")
                                .param("page", "1")
                                .param("size", "10")
                                .with(user(mentorPrincipal))
                )
                .andDo(print());

        // Then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.modificationRequests").isArray())
                .andExpect(jsonPath("$.data.modificationRequests[0].status").value("APPROVED"));
    }

    @Test
    @DisplayName("다른 회원의 멘토 정보 수정 요청 조회 시 실패")
    @WithMockUser(roles = "MENTOR")
    void getModificationRequestsFailWithForbidden() throws Exception {
        // When - 다른 사용자의 ID로 요청
        ResultActions resultActions = mvc
                .perform(
                        get("/api/mentor/" + testMemberId + "/modification-requests") // 다른 회원의 ID
                                .param("page", "1")
                                .param("size", "10")
                                .with(user(mentorPrincipal)) // 테스트 멘토로 로그인
                )
                .andDo(print());

        // Then
        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.message").value("해당 멘토 정보 수정 요청을 조회할 권한이 없습니다."));
    }

    @Test
    @DisplayName("잘못된 페이지 매개변수로 멘토 정보 수정 요청 조회 시 실패")
    @WithMockUser(roles = "MENTOR")
    void getModificationRequestsFailWithInvalidPage() throws Exception {
        // When - 유효하지 않은 페이지 번호로 요청
        ResultActions resultActions = mvc
                .perform(
                        get("/api/mentor/" + testMentorId + "/modification-requests")
                                .param("page", "0") // 1 미만의 페이지 번호
                                .param("size", "10")
                                .with(user(mentorPrincipal))
                )
                .andDo(print());

        // Then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.data.page").value("페이지 번호는 1 이상이어야 합니다."));
    }

    @Test
    @DisplayName("잘못된 상태 매개변수로 멘토 정보 수정 요청 조회 시 실패")
    @WithMockUser(roles = "MENTOR")
    void getModificationRequestsFailWithInvalidStatus() throws Exception {
        // When - 유효하지 않은 상태값으로 요청
        ResultActions resultActions = mvc
                .perform(
                        get("/api/mentor/" + testMentorId + "/modification-requests")
                                .param("status", "INVALID_STATUS") // 유효하지 않은 상태값
                                .param("page", "1")
                                .param("size", "10")
                                .with(user(mentorPrincipal))
                )
                .andDo(print());

        // Then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.data.status").value("유효하지 않은 상태값입니다."));
    }
}