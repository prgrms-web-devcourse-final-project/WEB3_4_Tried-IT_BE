package com.dementor.apply.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

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

import com.dementor.domain.apply.dto.request.ApplyRequest;
import com.dementor.domain.apply.entity.Apply;
import com.dementor.domain.apply.entity.ApplyStatus;
import com.dementor.domain.apply.repository.ApplyRepository;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.entity.UserRole;
import com.dementor.domain.member.repository.MemberRepository;
import com.dementor.domain.mentoringclass.entity.MentoringClass;
import com.dementor.domain.mentoringclass.repository.MentoringClassRepository;
import com.dementor.global.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dementor.domain.mentor.entity.Mentor;
import com.dementor.domain.mentor.repository.MentorRepository;
import com.dementor.domain.job.entity.Job;
import com.dementor.domain.job.repository.JobRepository;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ApplyControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private MentoringClassRepository mentoringClassRepository;

	@Autowired
	private ApplyRepository applyRepository;

	@Autowired
	private MentorRepository mentorRepository;

	@Autowired
	private JobRepository jobRepository;

	private Member testMentee;
	private Member testMentor;
	private Long testMentoringClassId;
	private MentoringClass testMentoringClass;
	private CustomUserDetails menteePrincipal;
	private CustomUserDetails mentorPrincipal;

	@BeforeEach
	void setUp() {
		// 테스트용 멘티 생성
		testMentee = Member.builder()
			.nickname("testMentee")
			.password("password")
			.name("테스트멘티")
			.email("123@1233.com")
			.userRole(UserRole.MENTEE)
			.build();
		memberRepository.save(testMentee);
		menteePrincipal = CustomUserDetails.of(testMentee);

		// 테스트용 멘토 생성
		testMentor = Member.builder()
			.nickname("testMentor")
			.password("password")
			.name("테스트멘토")
			.email("1234@1233.com")
			.userRole(UserRole.MENTOR)
			.build();
		memberRepository.save(testMentor);
		mentorPrincipal = CustomUserDetails.of(testMentor);

		Job job = Job.builder()
			.name("백엔드")
			.build();
		job = jobRepository.save(job);
		
		// 멘토 객체 생성
		Mentor mentor = Mentor.builder()
			.member(testMentor)
			.name("테스트멘토")
			.job(job)
			.career(3)
			.phone("010-1234-5678")
			.introduction("테스트 멘토 소개")
			.build();
		mentor = mentorRepository.save(mentor);

		// 멘토링 클래스 생성
		testMentoringClass = new MentoringClass();
		testMentoringClass.setTitle("테스트 멘토링");
		testMentoringClass.setStack("Java, Spring");
		testMentoringClass.setContent("테스트 멘토링 내용입니다");
		testMentoringClass.setPrice(50000);
		testMentoringClass.setMentor(mentor);

		testMentoringClass = mentoringClassRepository.save(testMentoringClass);
		testMentoringClassId = testMentoringClass.getId();
	}

	@Test
	@DisplayName("멘토링 신청 성공 - 멘티")
	@WithMockUser(roles = "MENTEE")
	void createApply1() throws Exception {

		ApplyRequest.ApplyCreateRequest request = new ApplyRequest.ApplyCreateRequest();
		request.setClassId(testMentoringClassId);
		request.setInquiry("멘티의 테스트 문의입니다");
		request.setSchedule(LocalDateTime.now().plusDays(1));


		ResultActions resultActions = mvc
			.perform(
				post("/api/apply")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
					.with(user(menteePrincipal))

			)
			.andDo(print());

		resultActions
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("201"))
			.andExpect(jsonPath("$.message").value("멘토링 신청이 완료되었습니다"))
			.andExpect(jsonPath("$.data.applymentId").exists());
	}

	@Test
	@DisplayName("멘토링 신청 성공 - 멘토")
	@WithMockUser(roles = "MENTOR")
	void createApply2() throws Exception {

		ApplyRequest.ApplyCreateRequest request = new ApplyRequest.ApplyCreateRequest();
		request.setClassId(testMentoringClassId);
		request.setInquiry("멘토의 테스트 문의입니다");
		request.setSchedule(LocalDateTime.now().plusDays(1));


		ResultActions resultActions = mvc
			.perform(
				post("/api/apply")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
					.with(user(mentorPrincipal))
			)
			.andDo(print());

		resultActions
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("201"))
			.andExpect(jsonPath("$.message").value("멘토링 신청이 완료되었습니다"))
			.andExpect(jsonPath("$.data.applymentId").exists());
	}

	@Test
	@DisplayName("멘토링 신청 취소 성공 - 멘티")
	@WithMockUser(roles = "MENTEE")
	void deleteApply() throws Exception {

		Apply testApply = Apply.builder()
			.mentoringClass(testMentoringClass)
			.member(testMentee)
			.inquiry("테스트용 문의")
			.applyStatus(ApplyStatus.PENDING)
			.schedule(LocalDateTime.now().plusDays(1))
			.build();

		testApply = applyRepository.save(testApply);

		ResultActions resultActions = mvc
			.perform(
				delete("/api/apply/" + testApply.getId())
					.with(user(menteePrincipal))
			)
			.andDo(print());

		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.message").value("멘토링 신청이 취소되었습니다"));
	}

	@Test
	@DisplayName("멘토링 신청 취소 성공 - 멘토")
	@WithMockUser(roles = "MENTOR")
	void deleteApply2() throws Exception {

		Apply testApply = Apply.builder()
			.mentoringClass(testMentoringClass)
			.member(testMentor)
			.inquiry("멘토의 테스트용 문의")
			.applyStatus(ApplyStatus.PENDING)
			.schedule(LocalDateTime.now().plusDays(1))
			.build();

		testApply = applyRepository.save(testApply);

		ResultActions resultActions = mvc
			.perform(
				delete("/api/apply/" + testApply.getId())
					.with(user(mentorPrincipal))
			)
			.andDo(print());


		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.message").value("멘토링 신청이 취소되었습니다"));
	}

	@Test
	@DisplayName("멘토링 신청 목록 조회 테스트 - 멘티")
	@WithMockUser(roles = "MENTEE")
	void getApplyListTest() throws Exception {

		for (int i = 0; i < 15; i++) {
			Apply apply = Apply.builder()
				.mentoringClass(testMentoringClass)
				.member(testMentee)
				.inquiry("조회 테스트용 문의 " + i)
				.applyStatus(ApplyStatus.PENDING)
				.schedule(LocalDateTime.now().plusDays(i + 1))
				.build();
			applyRepository.save(apply);
		}

		// 1페이지 조회 테스트
		ResultActions resultActions = mvc
			.perform(
				get("/api/apply")
					.param("page", "1")
					.param("size", "10")
					.with(user(menteePrincipal))
			)
			.andDo(print());

		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.message").value("멘토링 신청 목록을 조회했습니다"))
			.andExpect(jsonPath("$.data.applyments").exists())
			.andExpect(jsonPath("$.data.applyments").isArray())
			.andExpect(jsonPath("$.data.applyments.length()").value(10))
			.andExpect(jsonPath("$.data.applyments[0].mentorId").exists())
			.andExpect(jsonPath("$.data.applyments[0].name").exists())
			.andExpect(jsonPath("$.data.pagination").exists())
			.andExpect(jsonPath("$.data.pagination.page").value(1))
			.andExpect(jsonPath("$.data.pagination.size").value(10))
			.andExpect(jsonPath("$.data.pagination.total_elements").value(15))
			.andExpect(jsonPath("$.data.pagination.total_pages").value(2));

		// 2페이지
		ResultActions page2Results = mvc
			.perform(
				get("/api/apply")
					.param("page", "2")
					.param("size", "10")
					.with(user(menteePrincipal))
			)
			.andDo(print());

		page2Results
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.data.applyments").exists())
			.andExpect(jsonPath("$.data.applyments").isArray())
			.andExpect(jsonPath("$.data.applyments.length()").value(5))
			.andExpect(jsonPath("$.data.applyments[0].mentorId").exists())
			.andExpect(jsonPath("$.data.applyments[0].name").exists())
			.andExpect(jsonPath("$.data.pagination.page").value(2))
			.andExpect(jsonPath("$.data.pagination.size").value(10))
			.andExpect(jsonPath("$.data.pagination.total_elements").value(15))
			.andExpect(jsonPath("$.data.pagination.total_pages").value(2));
	}

	@Test
	@DisplayName("멘토링 신청 목록 조회 테스트 - 멘토")
	@WithMockUser(roles = "MENTOR")
	void getApplyListTest2() throws Exception {
		
		for (int i = 0; i < 15; i++) {
			Apply apply = Apply.builder()
				.mentoringClass(testMentoringClass)
				.member(testMentor)
				.inquiry("멘토 자신의 신청 테스트 " + i)
				.applyStatus(ApplyStatus.PENDING)
				.schedule(LocalDateTime.now().plusDays(i + 1))
				.build();
			applyRepository.save(apply);
		}
		
		// 1페이지
		ResultActions resultActions = mvc
			.perform(
				get("/api/apply")
					.param("page", "1")
					.param("size", "10")
					.with(user(mentorPrincipal))
			)
			.andDo(print());

		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.message").value("멘토링 신청 목록을 조회했습니다"))
			.andExpect(jsonPath("$.data.applyments").exists())
			.andExpect(jsonPath("$.data.applyments").isArray())
			.andExpect(jsonPath("$.data.applyments.length()").value(10))
			.andExpect(jsonPath("$.data.applyments[0].mentorId").exists())
			.andExpect(jsonPath("$.data.applyments[0].name").exists())
			.andExpect(jsonPath("$.data.pagination").exists())
			.andExpect(jsonPath("$.data.pagination.page").value(1))
			.andExpect(jsonPath("$.data.pagination.size").value(10))
			.andExpect(jsonPath("$.data.pagination.total_elements").value(15))
			.andExpect(jsonPath("$.data.pagination.total_pages").value(2));
			
		// 2페이지
		ResultActions page2Results = mvc
			.perform(
				get("/api/apply")
					.param("page", "2")
					.param("size", "10")
					.with(user(mentorPrincipal))
			)
			.andDo(print());

		page2Results
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.data.applyments").exists())
			.andExpect(jsonPath("$.data.applyments").isArray())
			.andExpect(jsonPath("$.data.applyments.length()").value(5))
			.andExpect(jsonPath("$.data.applyments[0].mentorId").exists())
			.andExpect(jsonPath("$.data.applyments[0].name").exists())
			.andExpect(jsonPath("$.data.pagination.page").value(2))
			.andExpect(jsonPath("$.data.pagination.size").value(10))
			.andExpect(jsonPath("$.data.pagination.total_elements").value(15))
			.andExpect(jsonPath("$.data.pagination.total_pages").value(2));
	}

}

