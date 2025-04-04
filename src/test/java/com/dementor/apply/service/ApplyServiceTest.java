package com.dementor.apply.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.dementor.domain.apply.dto.request.ApplyCreateRequest;
import com.dementor.domain.apply.dto.response.ApplyIdResponse;
import com.dementor.domain.apply.dto.response.ApplyPageResponse;
import com.dementor.domain.apply.entity.Apply;
import com.dementor.domain.apply.entity.ApplyStatus;
import com.dementor.domain.apply.exception.ApplyErrorCode;
import com.dementor.domain.apply.exception.ApplyException;
import com.dementor.domain.apply.repository.ApplyRepository;
import com.dementor.domain.apply.service.ApplyService;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.entity.UserRole;
import com.dementor.domain.member.repository.MemberRepository;
import com.dementor.domain.mentoringclass.entity.MentoringClass;
import com.dementor.domain.mentoringclass.repository.MentoringClassRepository;
import com.dementor.domain.mentor.entity.Mentor;
import com.dementor.domain.mentor.repository.MentorRepository;
import com.dementor.domain.job.entity.Job;
import com.dementor.domain.job.repository.JobRepository;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class ApplyServiceTest {

	@Autowired
	private ApplyService applyService;

	@Autowired
	private MentoringClassRepository mentoringClassRepository;

	@Autowired
	private ApplyRepository applyRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private MentorRepository mentorRepository;

	@Autowired
	private JobRepository jobRepository;

	private MentoringClass mentoringClass;
	private Long mentoringClassId;
	private Member testMember;
	private Member testMentor;

	@BeforeEach
	void setUp() {
		// 테스트 멘티 생성
		testMember = Member.builder()
			.email("test@test.com")
			.password("password")
			.nickname("test")
			.name("test")
			.userRole(UserRole.MENTEE)
			.build();
		testMember = memberRepository.save(testMember);

		// 테스트용 멘토 추가
		testMentor = Member.builder()
			.email("mentor@test.com")
			.password("password")
			.nickname("testMentor")
			.name("테스트멘토")
			.userRole(UserRole.MENTOR)
			.build();
		testMentor = memberRepository.save(testMentor);
		
		// Job 생성
		Job job = Job.builder()
			.name("백엔드")
			.build();
		job = jobRepository.save(job);
		
		// 멘토 객체 생성
		Mentor mentor = Mentor.builder()
			.member(testMentor)
			.job(job)
			.name("테스트멘토")
			.currentCompany("테스트 회사")
			.career(3)
			.phone("010-1234-5678")
			.email("mentor@example.com")
			.introduction("테스트 멘토 소개")
			.bestFor("테스트 특기")
			.approvalStatus(Mentor.ApprovalStatus.APPROVED)
			.modificationStatus(Mentor.ModificationStatus.NONE)
			.build();
		mentor = mentorRepository.save(mentor);

		// 멘토링 클래스 생성
		MentoringClass mentoring = new MentoringClass();
		mentoring.setTitle("테스트 멘토링");
		mentoring.setContent("테스트 내용");
		mentoring.setPrice(10000);
		mentoring.setStack("Java");
		mentoring.setMentor(mentor); // 멘토 설정
		
		this.mentoringClass = mentoringClassRepository.save(mentoring);
		this.mentoringClassId = this.mentoringClass.getId();
	}

	@Test
	@DisplayName("멘토링 신청 성공")
	void createApplySuccess() {

		ApplyCreateRequest request = new ApplyCreateRequest();
		request.setClassId(mentoringClassId);
		request.setInquiry("테스트 문의입니다");
		request.setSchedule(LocalDateTime.now().plusDays(1));


		ApplyIdResponse result = applyService.createApply(request, testMember.getId());


		assertNotNull(result);
		assertNotNull(result.getApplymentId());


		Apply savedApply = applyRepository.findById(result.getApplymentId()).orElse(null);
		assertNotNull(savedApply);
		assertEquals("테스트 문의입니다", savedApply.getInquiry());
		assertEquals(ApplyStatus.PENDING, savedApply.getApplyStatus());
		assertEquals(mentoringClassId, savedApply.getMentoringClass().getId());
	}

	@Test
	@DisplayName("존재하지 않는 멘토링 클래스로 신청 시 예외 발생")
	void createApplyFail1() {

		ApplyCreateRequest request = new ApplyCreateRequest();
		request.setClassId(99999L);
		request.setInquiry("테스트 문의입니다");
		request.setSchedule(LocalDateTime.now().plusDays(1));


		assertThrows(IllegalArgumentException.class, () -> {
			applyService.createApply(request, testMember.getId());
		});
	}

	@Test
	@DisplayName("멘토링 일정을 입력하지 않고 신청 시 예외 발생")
	void createApplyFail2() {

		ApplyCreateRequest request = new ApplyCreateRequest();
		request.setClassId(mentoringClassId);
		request.setInquiry("테스트 문의입니다");
		request.setSchedule(null);

		ApplyException exception = assertThrows(ApplyException.class, () -> {
			applyService.createApply(request, testMember.getId());
		});

		assertEquals(ApplyErrorCode.SCHEDULE_REQUIRED, exception.getErrorCode());
		assertEquals("멘토링 일정을 선택해야 합니다.", exception.getMessage());
	}

	@Test
	@DisplayName("멘토링 신청 취소 성공")
	void deleteApplySuccess() {

		ApplyCreateRequest request = new ApplyCreateRequest();
		request.setClassId(mentoringClassId);
		request.setInquiry("취소 테스트용 문의입니다");
		request.setSchedule(LocalDateTime.now().plusDays(1));

		ApplyIdResponse result = applyService.createApply(request, testMember.getId());
		Long applyId = result.getApplymentId();

		applyService.deleteApply(applyId, testMember.getId());


		Apply deletedApply = applyRepository.findById(applyId).orElse(null);
		assertNull(deletedApply, "멘토링 신청 취소가 되지 않았습니다.");
	}

	@Test
	@DisplayName("권한 없는 사용자가 멘토링 신청 취소 시 예외 발생")
	void deleteApplyFail() {

		ApplyCreateRequest request = new ApplyCreateRequest();
		request.setClassId(mentoringClassId);
		request.setInquiry("취소 테스트용 문의입니다");
		request.setSchedule(LocalDateTime.now().plusDays(1));

		ApplyIdResponse result = applyService.createApply(request, testMember.getId());
		Long applyId = result.getApplymentId();

		Member anotherMember = Member.builder()
			.email("another@test.com")
			.password("password")
			.nickname("another")
			.name("another")
			.userRole(UserRole.MENTEE)
			.build();
		anotherMember = memberRepository.save(anotherMember);

		Member AnotherMember = anotherMember;
		ApplyException exception = assertThrows(ApplyException.class, () -> {
			applyService.deleteApply(applyId, AnotherMember.getId());
		});

		assertEquals(ApplyErrorCode.NOT_YOUR_APPLY, exception.getErrorCode());
	}

	@Test
	@DisplayName("멘토링 신청 목록 조회 성공 - 페이징")
	void getApplyListSuccess() {

		for (int i = 0; i < 15; i++) {
			Apply apply = Apply.builder()
				.mentoringClass(mentoringClass)
				.member(testMember)
				.inquiry("테스트 문의 " + i)
				.applyStatus(ApplyStatus.PENDING)
				.schedule(LocalDateTime.now().plusDays(i % 5 + 1))
				.build();
			applyRepository.save(apply);
		}


		ApplyPageResponse page1Result = applyService.getApplyList(testMember.getId(), 0, 10);


		assertEquals(15L, page1Result.getPagination().getTotal_elements());
		assertEquals(2, page1Result.getPagination().getTotal_pages());
		assertEquals(10, page1Result.getApplyments().size());

		ApplyPageResponse page2Result = applyService.getApplyList(testMember.getId(), 1, 10);

		assertEquals(15L, page2Result.getPagination().getTotal_elements());
		assertEquals(2, page2Result.getPagination().getTotal_pages());
		assertEquals(5, page2Result.getApplyments().size());
	}


}

