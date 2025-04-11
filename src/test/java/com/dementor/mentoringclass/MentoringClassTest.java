package com.dementor.mentoringclass;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import com.dementor.domain.mentor.entity.ModificationStatus;
import com.dementor.domain.mentor.repository.MentorRepository;
import com.dementor.domain.mentoringclass.dto.DayOfWeek;
import com.dementor.domain.mentoringclass.dto.request.MentoringClassCreateRequest;
import com.dementor.domain.mentoringclass.dto.request.MentoringClassUpdateRequest;
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
	private CustomUserDetails mentorPrincipal;

	@BeforeEach
	void setUp() {
		Member mentor = Member.builder()
			.email("test@test.com")
			.password("password")
			.nickname("테스트 멘토")
			.name("테스트 멘토")
			.userRole(UserRole.MENTOR)
			.build();
		mentor = memberRepository.save(mentor);
		mentorPrincipal = CustomUserDetails.of(mentor);

		Job job = Job.builder()
			.name("백엔드 개발자")
			.build();
		job = jobRepository.save(job);

		Mentor mentorEntity = Mentor.builder()
			.member(mentor)
			.job(job)
			.name("테스트 멘토")
			.currentCompany("테스트 회사")
			.career(5)
			.phone("010-1234-5678")
			.email("mentor@example.com")
			.introduction("테스트 멘토 소개")
			.modificationStatus(ModificationStatus.NONE)
			.build();
		mentorEntity = mentorRepository.save(mentorEntity);

		MentoringClass mentoringClass = MentoringClass.builder()
			.title("테스트 수업")
			.stack("Spring Boot")
			.content("테스트용 수업 내용")
			.price(50000)
			.mentor(mentorEntity)
			.build();
		mentoringClass = mentoringClassRepository.save(mentoringClass);
		testClassId = mentoringClass.getId();

		Schedule schedule = Schedule.builder()
			.dayOfWeek(DayOfWeek.MONDAY)
			.time("10:00-11:00")
			.mentoringClassId(testClassId)
			.build();
		schedule = scheduleRepository.save(schedule);

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
				.param("page", "0")
				.param("size", "10")
				.param("sortBy", "createdAt")
				.param("order", "DESC")
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").value("멘토링 수업 조회 성공"))
			.andExpect(jsonPath("$.data.content").isArray())
			.andExpect(jsonPath("$.data.totalElements").isNumber())
			.andExpect(jsonPath("$.data.totalPages").isNumber())
			.andExpect(jsonPath("$.data.size").value(10))
			.andExpect(jsonPath("$.data.number").value(0));
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
			new String[] {"Spring Boot", "Java", "MySQL"},  // 기술 스택 배열로 정의
			"스프링 부트 기초부터 실전까지",
			"스프링 부트 완전 정복",
			50000,
			List.of(
				new ScheduleRequest(DayOfWeek.TUESDAY, "10:00-11:00")
			)
		);

		// when & then
		mockMvc.perform(post("/api/class")
				.contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("201"))
			.andExpect(jsonPath("$.message").value("멘토링 클래스 생성 성공"))
			.andExpect(jsonPath("$.data.classId").exists())
			.andExpect(jsonPath("$.data.mentor").exists())
			.andExpect(jsonPath("$.data.stack").exists())
			.andExpect(jsonPath("$.data.content").exists())
			.andExpect(jsonPath("$.data.title").exists())
			.andExpect(jsonPath("$.data.price").exists())
			.andExpect(jsonPath("$.data.schedules").exists());
	}

	@Test
	void updateMentoringClass() throws Exception {
		// given
		MentoringClassUpdateRequest request = new MentoringClassUpdateRequest(
			"수정된 수업 제목",
			"수정된 수업 내용",
			100000,
			new String[] {"Spring Boot", "Java", "MySQL", "JPA"},  // 기술 스택 배열로 정의
			new ScheduleRequest(DayOfWeek.WEDNESDAY, "14:00-16:00")
		);

		// when & then
		mockMvc.perform(put("/api/class/{class_id}", testClassId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").value("멘토링 클래스 수정 성공"))
			.andExpect(jsonPath("$.data.title").value("수정된 수업 제목"))
			.andExpect(jsonPath("$.data.content").value("수정된 수업 내용"))
			.andExpect(jsonPath("$.data.price").value(100000))
			.andExpect(jsonPath("$.data.schedule.dayOfWeek").value("WEDNESDAY"))
			.andExpect(jsonPath("$.data.schedule.time").value("14:00-16:00"));
	}

	@Test
	void deleteMentoringClass() throws Exception {
		// when & then
		mockMvc.perform(delete("/api/class/{class_id}", testClassId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("204"))
			.andExpect(jsonPath("$.message").value("멘토링 수업 삭제 성공"));
	}

}
