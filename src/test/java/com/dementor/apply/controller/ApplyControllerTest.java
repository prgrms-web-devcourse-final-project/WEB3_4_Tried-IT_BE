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
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.entity.UserRole;
import com.dementor.domain.member.repository.MemberRepository;
import com.dementor.domain.mentoringclass.entity.MentoringClass;
import com.dementor.domain.mentoringclass.repository.MentoringClassRepository;
import com.dementor.global.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	private Member testMentee; // 멘티(일반 회원)
	private Member testMentor; // 멘토
	private Long testMentoringClassId;
	private CustomUserDetails menteePrincipal;
	private CustomUserDetails mentorPrincipal;

	@BeforeEach
	void setUp() {
		// 테스트용 멘티 생성
		testMentee = Member.builder()
			.nickname("testMentee")
			.password("password")
			.nickname("테스트멘티")
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
			.nickname("테스트멘토")
			.name("테스트멘토")
			.email("1234@1233.com")
			.userRole(UserRole.MENTOR)
			.build();
		memberRepository.save(testMentor);
		mentorPrincipal = CustomUserDetails.of(testMentor);

		// 멘토링 클래스
		MentoringClass testMentoringClass = new MentoringClass();
		testMentoringClass.setTitle("테스트 멘토링");
		testMentoringClass.setStack("Java, Spring");
		testMentoringClass.setContent("테스트 멘토링 내용입니다");
		testMentoringClass.setPrice(50000);

		testMentoringClass = mentoringClassRepository.save(testMentoringClass);
		testMentoringClassId = testMentoringClass.getId();
	}

	@Test
	@DisplayName("멘티가 멘토링 신청 성공")
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
			.andExpect(jsonPath("$.data.applyment_id").exists());
	}

	@Test
	@DisplayName("멘토가 멘토링 신청 성공")
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
			.andExpect(jsonPath("$.data.applyment_id").exists());
	}

}

