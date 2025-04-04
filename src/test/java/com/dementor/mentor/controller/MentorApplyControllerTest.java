package com.dementor.mentor.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import com.dementor.domain.mentor.repository.MentorRepository;
import com.dementor.domain.mentoringclass.entity.MentoringClass;
import com.dementor.domain.mentoringclass.repository.MentoringClassRepository;
import com.dementor.global.security.CustomUserDetails;

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

	private CustomUserDetails mentorPrincipal;
	private List<Apply> testApplies = new ArrayList<>();

	@BeforeEach
	void setUp() {
		// 테스트용 멘토 생성
		Member testMentor = Member.builder()
			.email("mentor@test.com")
			.password("password")
			.nickname("testMentor")
			.name("테스트멘토")
			.userRole(UserRole.MENTOR)
			.build();
		testMentor = memberRepository.save(testMentor);
		mentorPrincipal = CustomUserDetails.of(testMentor);

		// 테스트용 멘티 생성
		Member testMentee = Member.builder()
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
			.bestFor("테스트 특기")
			.introduction("테스트 멘토 소개")
			.approvalStatus(Mentor.ApprovalStatus.APPROVED)
			.modificationStatus(Mentor.ModificationStatus.NONE)  
			.build();
		mentor = mentorRepository.save(mentor);

		// 멘토링 클래스 생성
		MentoringClass mentoringClass = MentoringClass.builder()
			.title("테스트 멘토링")
			.content("테스트 내용")
			.price(50000)
			.stack("Java, Spring")
			.mentor(mentor)
			.build();
		mentoringClass = mentoringClassRepository.save(mentoringClass);


		for (int i = 0; i < 15; i++) {
			Apply apply = Apply.builder()
				.mentoringClass(mentoringClass)
				.member(testMentee)
				.inquiry("테스트 신청 " + i)
				.applyStatus(ApplyStatus.PENDING)
				.schedule(LocalDateTime.now().plusDays(i % 7 + 1))
				.build();
			testApplies.add(applyRepository.save(apply));
		}
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
			.andExpect(jsonPath("$.data.applyments[0].applymentId").exists())
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
	@DisplayName("승인되지 않은 멘토가 신청된 목록 조회")
	@WithMockUser(roles = "MENTOR")
	void getApplyByMentor2() throws Exception {

		Member unapprovedMentorMember = Member.builder()
			.email("unapproved@test.com")
			.password("password")
			.nickname("unapprovedMentor")
			.name("미승인멘토")
			.userRole(UserRole.MENTOR)
			.build();
		unapprovedMentorMember = memberRepository.save(unapprovedMentorMember);

		Job job = jobRepository.findAll().get(0);

		Mentor unapprovedMentor = Mentor.builder()
			.member(unapprovedMentorMember)
			.name("미승인멘토")
			.job(job)
			.career(1)
			.phone("010-9999-8888")
			.introduction("미승인 멘토 소개")
			.approvalStatus(Mentor.ApprovalStatus.PENDING)
			.build();
		mentorRepository.save(unapprovedMentor);

		CustomUserDetails unapprovedMentorPrincipal = CustomUserDetails.of(unapprovedMentorMember);

		// API 호출
		mvc.perform(
				get("/api/mentor/apply")
					.param("page", "1")
					.param("size", "10")
					.with(user(unapprovedMentorPrincipal))
			)
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value(403))
			.andExpect(jsonPath("$.message").value("승인되지 않은 멘토는 신청 목록을 조회할 수 없습니다"))
			.andDo(print());
	}
}