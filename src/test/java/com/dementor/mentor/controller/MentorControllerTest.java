package com.dementor.mentor.controller;

import com.dementor.domain.job.entity.Job;
import com.dementor.domain.job.repository.JobRepository;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.entity.UserRole;
import com.dementor.domain.member.repository.MemberRepository;
import com.dementor.domain.mentor.dto.request.MentorApplyProposalRequest;
import com.dementor.domain.mentor.entity.Mentor;
import com.dementor.domain.mentor.entity.ModificationStatus;
import com.dementor.domain.mentor.repository.MentorRepository;
import com.dementor.domain.mentorapplyproposal.repository.MentorApplyProposalRepository;
import com.dementor.domain.mentoreditproposal.dto.MentorEditProposalRequest;
import com.dementor.domain.mentoreditproposal.entity.MentorEditProposal;
import com.dementor.domain.mentoreditproposal.entity.MentorEditProposalStatus;
import com.dementor.domain.mentoreditproposal.repository.MentorEditProposalRepository;
import com.dementor.global.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
	private MentorEditProposalRepository mentorEditProposalRepository;

	@Autowired
	private MentorApplyProposalRepository mentorApplyProposalRepository;

	@BeforeEach
	void setUp() {
		// 기존 데이터 정리
		mentorEditProposalRepository.deleteAll();
		mentorApplyProposalRepository.deleteAll();
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
			.modificationStatus(ModificationStatus.NONE)
			.build();
		mentorRepository.save(mentorInfo);
	}

	@Test
	@Order(1)
	@DisplayName("멘토 지원 성공")
	@WithMockUser(roles = "MENTEE")
	void applyMentorSuccess() throws Exception {
		// Given
		MentorApplyProposalRequest.MentorApplyProposalRequestDto requestDto =
			new MentorApplyProposalRequest.MentorApplyProposalRequestDto(
				testMemberId,
				testMember.getName(),
				testJobId,
				"01012345678",
				"testmember@test.com",
				5,
				"테스트 회사",
				"테스트 자기소개",
				null
			);

		// Multipart 본문 중 하나로 보낼 JSON
		MockMultipartFile jsonPart = new MockMultipartFile(
			"mentorApplyData", // controller의 @RequestPart("mentorApplyData")와 일치
			null,
			"application/json",
			objectMapper.writeValueAsBytes(requestDto)
		);

		// When
		ResultActions resultActions = mvc
			.perform(MockMvcRequestBuilders.multipart("/api/mentor")
				.file(jsonPart)
				.with(request -> {
					request.setMethod("POST"); // multipart는 기본적으로 POST 아님
					return request;
				})
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
	@Order(2)
	@DisplayName("멘토 정보 수정 성공")
	@WithMockUser(roles = "MENTOR")
	void updateMentorSuccess() throws Exception {
		// Given
		MentorEditProposalRequest requestDto = new MentorEditProposalRequest(
			testJobId,                   // jobId
			8,                    // career
			"업데이트 회사",       // currentCompany
			"업데이트된 자기소개", // introduction
			null                  // attachmentId
		);

		// JSON 데이터를 multipart로 보내기 위한 MockMultipartFile
		MockMultipartFile jsonPart = new MockMultipartFile(
			"mentorUpdateData", // @RequestPart("mentorUpdateData")와 일치해야 함
			null,
			"application/json",
			objectMapper.writeValueAsBytes(requestDto)
		);

		// When
		ResultActions resultActions = mvc
			.perform(MockMvcRequestBuilders.multipart("/api/mentor/" + testMentorId)
				.file(jsonPart)
				.with(request -> {
					request.setMethod("PUT"); // PUT으로 강제 설정
					return request;
				})
				.with(user(mentorPrincipal))
			)
			.andDo(print());
		// Then
		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.message").value("멘토 정보 수정 요청에 성공했습니다."))
			.andExpect(jsonPath("$.data.memberId").value(testMentorId))
			.andExpect(jsonPath("$.data.status").value("PENDING"));
	}

	@Test
	@Order(3)
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
			.andExpect(jsonPath("$.data.memberId").value(testMentorId))
			.andExpect(jsonPath("$.data.name").value("테스트멘토"))
			.andExpect(jsonPath("$.data.jobName").value("백엔드 개발자"));
	}

	@Test
	@Order(4)
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
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.message").value("해당 멘토를 찾을 수 없습니다: " + nonExistentMentorId));
	}

	@Test
	@Order(5)
	@DisplayName("멘토 정보 수정 요청 조회 성공")
	@WithMockUser(roles = "MENTOR")
	void getModificationRequestsSuccess() throws Exception {
		// Given
		// 수정 요청을 생성 (이미 setUp 메서드에서 testMentor가 APPROVED 상태)
		MentorEditProposal modification = MentorEditProposal.builder()
			.member(testMentor)
			.career(8)
			.currentCompany("변경된 회사")
			.job(testJob)
			.introduction("변경된 자기소개")
			.status(MentorEditProposalStatus.PENDING)
			.build();
		mentorEditProposalRepository.save(modification);

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
	@Order(6)
	@DisplayName("멘토 정보 수정 요청 조회 - 상태별 필터링 성공")
	@WithMockUser(roles = "MENTOR")
	void getModificationRequestsWithStatusFilterSuccess() throws Exception {
		// Given
		// PENDING 상태의 수정 요청 생성
		MentorEditProposal pendingModification = MentorEditProposal.builder()
			.member(testMentor)
			.career(8)
			.currentCompany("변경된 회사")
			.job(testJob)
			.introduction("변경된 자기소개")
			.status(MentorEditProposalStatus.PENDING)
			.build();
		mentorEditProposalRepository.save(pendingModification);

		// APPROVED 상태의 수정 요청 생성
		MentorEditProposal approvedModification = MentorEditProposal.builder()
			.member(testMentor)
			.career(5)
			.currentCompany("변경된 회사")
			.job(testJob)
			.introduction("변경된 자기소개")
			.status(MentorEditProposalStatus.APPROVED)
			.build();
		mentorEditProposalRepository.save(approvedModification);

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
	@Order(7)
	@DisplayName("멘토가 아닌 회원의 ID로 정보 조회 시 실패")
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
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.message").value("해당 멘토를 찾을 수 없습니다: " + testMemberId));
	}

	@Test
	@Order(8)
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
	@Order(9)
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