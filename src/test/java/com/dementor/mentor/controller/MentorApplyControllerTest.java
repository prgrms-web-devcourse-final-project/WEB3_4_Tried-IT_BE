package com.dementor.mentor.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
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

import com.dementor.domain.apply.entity.Apply;
import com.dementor.domain.apply.entity.ApplyStatus;
import com.dementor.domain.apply.repository.ApplyRepository;
import com.dementor.domain.job.entity.Job;
import com.dementor.domain.job.repository.JobRepository;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.entity.UserRole;
import com.dementor.domain.member.repository.MemberRepository;
import com.dementor.domain.mentor.entity.Mentor;
import com.dementor.domain.mentor.entity.ModificationStatus;
import com.dementor.domain.mentor.repository.MentorRepository;
import com.dementor.domain.mentoringclass.entity.MentoringClass;
import com.dementor.domain.mentoringclass.repository.MentoringClassRepository;
import com.dementor.global.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class MentorApplyControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private MentorRepository mentorRepository;

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private MentoringClassRepository mentoringClassRepository;

	@Autowired
	private ApplyRepository applyRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private CustomUserDetails mentorPrincipal;
	private List<Apply> testApplies = new ArrayList<>();
	private Member testMentee;
	private MentoringClass testMentoringClass;
	private Member testMentor;

	@BeforeEach
	void setUp() {

		// 테스트용 멘토 생성
		testMentor = Member.builder()
			.email("mentor@test.com")
			.password("password")
			.nickname("testMentor")
			.name("테스트멘토")
			.userRole(UserRole.MENTOR)
			.build();
		testMentor = memberRepository.save(testMentor);
		mentorPrincipal = CustomUserDetails.of(testMentor);

		// 테스트용 멘티 생성
		testMentee = Member.builder()
			.email("mentee@test.com")
			.password("password")
			.nickname("testMentee")
			.name("테스트멘티")
			.userRole(UserRole.MENTEE)
			.build();
		testMentee = memberRepository.save(testMentee);

		// 직업 생성
		Job job = Job.builder()
			.name("개발자")
			.build();
		job = jobRepository.save(job);

		// 멘토 생성
		Mentor mentor = Mentor.builder()
			.member(testMentor)
			.name("테스트멘토")
			.job(job)
			.career(3)
			.phone("010-1234-5678")
			.email("mentor@example.com")
			.currentCompany("테스트 회사")
			.introduction("테스트 멘토 소개")
			.modificationStatus(ModificationStatus.NONE)
			.build();
		mentor = mentorRepository.save(mentor);

		// 멘토링 클래스 생성
		testMentoringClass = MentoringClass.builder()
			.title("테스트 멘토링")
			.content("테스트 내용")
			.price(50000)
			.stack("Java, Spring")
			.mentor(mentor)
			.build();
		testMentoringClass = mentoringClassRepository.save(testMentoringClass);

		for (int i = 0; i < 15; i++) {
			Apply apply = Apply.builder()
				.mentoringClass(testMentoringClass)
				.member(testMentee)
				.inquiry("테스트 신청 " + i)
				.applyStatus(ApplyStatus.PENDING)
				.schedule(LocalDateTime.now().plusDays(i % 7 + 1))
				.build();
			testApplies.add(applyRepository.save(apply));
		}
	}

	@AfterEach
	void tearDown() {
		mentorPrincipal = null;
		testMentee = null;
		testApplies = null;
		testMentoringClass = null;
		testMentor = null;
	}

	@Test
	@DisplayName("신청된 목록 조회 테스트 - 멘토")
	@WithMockUser(roles = "MENTOR")
	void getApplyByMentor() throws Exception {
		// 1페이지 조회
		ResultActions page1Actions = mvc
			.perform(
				get("/api/mentor/apply")
					.param("page", "1")
					.param("size", "10")
					.with(user(mentorPrincipal))
			)
			.andDo(print());

		page1Actions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.message").value("신청된 목록을 조회했습니다"))
			.andExpect(jsonPath("$.data.applyments").exists())
			.andExpect(jsonPath("$.data.applyments").isArray())
			.andExpect(jsonPath("$.data.applyments.length()").value(10))
			.andExpect(jsonPath("$.data.applyments[0].applyId").exists())
			.andExpect(jsonPath("$.data.applyments[0].memberId").exists())
			.andExpect(jsonPath("$.data.applyments[0].nickname").exists())
			.andExpect(jsonPath("$.data.pagination").exists())
			.andExpect(jsonPath("$.data.pagination.page").value(1))
			.andExpect(jsonPath("$.data.pagination.size").value(10))
			.andExpect(jsonPath("$.data.pagination.total_elements").value(15))
			.andExpect(jsonPath("$.data.pagination.total_pages").value(2));

		// 2페이지 조회
		ResultActions page2Actions = mvc
			.perform(
				get("/api/mentor/apply")
					.param("page", "2")
					.param("size", "10")
					.with(user(mentorPrincipal))
			)
			.andDo(print());

		page2Actions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.applyments.length()").value(5))
			.andExpect(jsonPath("$.data.pagination.page").value(2));
	}

	@Test
	@DisplayName("멘토링 신청 승인 테스트")
	@WithMockUser(roles = "MENTOR")
	void approveApply() throws Exception {
		// 1번 신청을 승인
		Long applyId = testApplies.get(0).getId();

		String requestJson = "{\"status\": \"APPROVED\"}";

		mvc.perform(
				put("/api/mentor/apply/" + applyId + "/status")
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestJson)
					.with(user(mentorPrincipal))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.message").value("멘토링 신청이 승인되었습니다."))
			.andExpect(jsonPath("$.data.applymentId").value(applyId))
			.andExpect(jsonPath("$.data.status").value("APPROVED"))
			.andDo(print());

		// DB에 실제로 변경되었는지 확인
		Apply updatedApply = applyRepository.findById(applyId).orElseThrow();
		assert updatedApply.getApplyStatus() == ApplyStatus.APPROVED;
	}

	@Test
	@DisplayName("멘토링 신청 거절 테스트")
	@WithMockUser(roles = "MENTOR")
	void rejectApply() throws Exception {
		// 2번 신청을 거절
		Long applyId = testApplies.get(1).getId();

		String requestJson = "{\"status\": \"REJECTED\"}";

		mvc.perform(
				put("/api/mentor/apply/" + applyId + "/status")
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestJson)
					.with(user(mentorPrincipal))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.message").value("멘토링 신청이 거절되었습니다."))
			.andExpect(jsonPath("$.data.applymentId").value(applyId))
			.andExpect(jsonPath("$.data.status").value("REJECTED"))
			.andDo(print());

		// DB에 실제로 변경되었는지 확인
		Apply updatedApply = applyRepository.findById(applyId).orElseThrow();
		assert updatedApply.getApplyStatus() == ApplyStatus.REJECTED;
	}

	@Test
	@DisplayName("존재하지 않는 신청 상태 변경 시도")
	@WithMockUser(roles = "MENTOR")
	void updateNonExistingApply() throws Exception {
		Long nonExistingApplyId = 9999L;

		String requestJson = "{\"status\": \"APPROVED\"}";

		mvc.perform(
				put("/api/mentor/apply/" + nonExistingApplyId + "/status")
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestJson)
					.with(user(mentorPrincipal))
			)
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.message").value("존재하지 않는 신청입니다."))
			.andDo(print());
	}

}