package com.dementor.mentor.service;

import com.dementor.domain.job.entity.Job;
import com.dementor.domain.job.repository.JobRepository;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.entity.UserRole;
import com.dementor.domain.member.repository.MemberRepository;
import com.dementor.domain.mentor.dto.request.MentorApplyProposalRequest;
import com.dementor.domain.mentor.dto.request.MentorChangeRequest;
import com.dementor.domain.mentor.dto.response.MentorChangeResponse;
import com.dementor.domain.mentor.dto.response.MentorInfoResponse;
import com.dementor.domain.mentor.entity.Mentor;
import com.dementor.domain.mentor.entity.ModificationStatus;
import com.dementor.domain.mentor.exception.MentorException;
import com.dementor.domain.mentor.repository.MentorRepository;
import com.dementor.domain.mentor.service.MentorService;
import com.dementor.domain.mentorapplyproposal.dto.response.ApplymentResponse;
import com.dementor.domain.mentorapplyproposal.entity.MentorApplyProposal;
import com.dementor.domain.mentorapplyproposal.entity.MentorApplyProposalStatus;
import com.dementor.domain.mentorapplyproposal.repository.MentorApplyProposalRepository;
import com.dementor.domain.mentoreditproposal.dto.MentorEditProposalRequest;
import com.dementor.domain.mentoreditproposal.dto.MentorEditUpdateRenewalResponse;
import com.dementor.domain.mentoreditproposal.entity.MentorEditProposal;
import com.dementor.domain.mentoreditproposal.entity.MentorEditProposalStatus;
import com.dementor.domain.mentoreditproposal.repository.MentorEditProposalRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MentorServiceTest {

	@Autowired
	private MentorService mentorService;

	@Autowired
	private MentorRepository mentorRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private JobRepository jobRepository;

	private Job testJob;
	private Member testMember;
	private Member testMentorMember;
	private Mentor testMentor;
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
			.email("test@example.com")
			.userRole(UserRole.MENTEE)
			.build();
		testMember = memberRepository.save(testMember);

		// 테스트용 멘토 회원 생성
		testMentorMember = Member.builder()
			.nickname("testMentor")
			.password("password")
			.name("테스트멘토")
			.email("mentor@example.com")
			.userRole(UserRole.MENTOR)
			.build();
		testMentorMember = memberRepository.save(testMentorMember);

		// 테스트용 직무 생성
		testJob = Job.builder()
			.name("백엔드 개발자")
			.build();
		testJob = jobRepository.save(testJob);

		// 테스트용 멘토 정보 생성 (수정/조회 테스트용)
		testMentor = Mentor.builder()
			.member(testMentorMember)
			.job(testJob)
			.name(testMentorMember.getName())
			.currentCompany("테스트 회사")
			.career(5)
			.phone("01012345678")
			.email("mentor@example.com")
			.introduction("테스트 자기소개")
			.modificationStatus(ModificationStatus.NONE)
			.build();
		testMentor = mentorRepository.save(testMentor);
	}

	@Test
	@Order(1)
	@DisplayName("멘토 지원 성공")
	void applyMentorSuccess() {
		// Given
		MentorApplyProposalRequest.MentorApplyProposalRequestDto requestDto =
			new MentorApplyProposalRequest.MentorApplyProposalRequestDto(
				testMember.getId(),
				testMember.getName(),
				testJob.getId(),
				"01012345678",
				"test@example.com",
				5,
				"테스트 회사",
				"테스트 자기소개"
			);

		// mock 파일 목록 (빈 리스트로 제공)
		List<MultipartFile> files = Collections.emptyList();

		// When
		ApplymentResponse response = mentorService.applyMentor(requestDto, files);

		// Then
		assertNotNull(response, "응답이 null이 아니어야 합니다.");
		assertEquals(testMember.getId(), response.memberId());
		assertEquals(testMember.getName(), response.name());
		assertEquals("PENDING", response.status());

		// 실제 저장된 엔티티 확인
		MentorApplyProposal savedApplication = mentorApplyProposalRepository.findByMemberId(testMember.getId())
				.orElse(null);
		assertNotNull(savedApplication, "멘토 지원 정보가 저장되지 않았습니다.");
		assertEquals("테스트 자기소개", savedApplication.getIntroduction());
		assertEquals(MentorApplyProposalStatus.PENDING, savedApplication.getStatus());

		// 승인 프로세스 - 지원 요청이 저장되어 있는 상태
		MentorApplyProposal application = mentorApplyProposalRepository.findLatestByMemberId(testMember.getId())
			.orElseThrow(() -> new AssertionError("지원 정보가 없습니다."));

		// 승인 처리
		application.updateStatus(MentorApplyProposalStatus.APPROVED);
		mentorApplyProposalRepository.save(application);

		// 멘토 생성
		Mentor mentor = application.toMentor();
		mentorRepository.save(mentor);

		// Then - 멘토 정보 검증
		Mentor savedMentor = mentorRepository.findById(testMember.getId()).orElse(null);
		assertNotNull(savedMentor, "승인된 멘토 정보가 저장되지 않았습니다.");
		assertEquals("테스트 자기소개", savedMentor.getIntroduction());
	}

	@Test
	@Order(2)
	@DisplayName("없는 회원으로 멘토 지원 시 예외 발생")
	void applyMentorFailMemberNotFound() {
		// Given
		Long nonExistingMemberId = 9999L;
		MentorApplyProposalRequest.MentorApplyProposalRequestDto requestDto =
			new MentorApplyProposalRequest.MentorApplyProposalRequestDto(
				nonExistingMemberId,
				"존재하지 않는 회원",
				testJob.getId(),
				"01012345678",
				"nonexistent@example.com",
				5,
				"테스트 회사",
				"테스트 자기소개"
			);

		List<MultipartFile> files = Collections.emptyList();

		// When & Then
		Exception exception = assertThrows(MentorException.class, () -> {
			mentorService.applyMentor(requestDto, files);
		});

		assertTrue(exception.getMessage().contains("회원을 찾을 수 없습니다"));
	}

	@Test
	@Order(3)
	@DisplayName("이미 멘토인 회원이 멘토 지원 시 예외 발생")
	void applyMentorFailAlreadyMentor() {
		// Given
		MentorApplyProposalRequest.MentorApplyProposalRequestDto requestDto =
			new MentorApplyProposalRequest.MentorApplyProposalRequestDto(
				testMentorMember.getId(),
				testMentorMember.getName(),
				testJob.getId(),
				"01012345678",
				"mentor@example.com",
				5,
				"테스트 회사",
				"테스트 자기소개"
			);

		List<MultipartFile> files = Collections.emptyList();

		// When & Then
		Exception exception = assertThrows(MentorException.class, () -> {
			mentorService.applyMentor(requestDto, files);
		});

		assertTrue(exception.getMessage().contains("이미 멘토로 등록된 사용자입니다"));
	}

	// 멘토 수정 요청 테스트
	@Test
	@Order(4)
	@DisplayName("멘토 정보 수정 요청 성공")
	@Transactional
	void createMentorUpdateRequestSuccess() {
		// Given
		MentorEditProposalRequest requestDto = new MentorEditProposalRequest(
			testJob.getId(),        // jobId
			8,                      // career
			"업데이트 회사",         // currentCompany
			"업데이트된 자기소개"    // introduction
		);

		List<MultipartFile> files = Collections.emptyList();

		// When
		MentorEditUpdateRenewalResponse response = mentorService.updateMentor(testMentor.getId(), requestDto, files);

		// Then
		assertNotNull(response, "응답이 null이 아니어야 합니다.");
		assertEquals(testMentor.getMember().getId(), response.memberId());
		assertEquals(MentorEditProposalStatus.PENDING, response.status());

		Mentor updatedMentor = mentorRepository.findById(testMentor.getId()).orElse(null);
		assertNotNull(updatedMentor, "멘토 정보가 조회되지 않습니다.");
		assertEquals(ModificationStatus.PENDING, updatedMentor.getModificationStatus());

		Job job = jobRepository.findById(testJob.getId())
			.orElseThrow(() -> new AssertionError("JOB이 없습니다."));
		;

		// 수정 요청 승인 프로세스 - 수정 요청이 저장되어 있는 상태
		MentorEditProposal modification = mentorEditProposalRepository.findLatestByMemberId(testMentor.getId())
			.orElseThrow(() -> new AssertionError("수정 요청이 없습니다."));

		// 승인 처리
		modification.updateStatus(MentorEditProposalStatus.APPROVED);
		mentorEditProposalRepository.save(modification);

		Mentor mentor = mentorRepository.findById(testMentor.getId()).orElseThrow();
		mentor.update(
			"업데이트 회사",
			8,
			job,
			"업데이트된 자기소개",
			ModificationStatus.APPROVED
		);
		mentor.updateModificationStatus(ModificationStatus.NONE);
		mentorRepository.save(mentor);

		// Then - 변경 확인
		Mentor finalMentor = mentorRepository.findById(testMentor.getId()).orElseThrow();
		assertEquals("업데이트된 자기소개", finalMentor.getIntroduction());
		assertEquals(8, finalMentor.getCareer());
		assertEquals(ModificationStatus.NONE, finalMentor.getModificationStatus());
	}

	@Test
	@Order(5)
	@DisplayName("존재하지 않는 멘토 정보 수정 시 예외 발생")
	void updateMentorFailMentorNotFound() {
		// Given
		final Long nonExistingMentorId = 9999L;
		MentorEditProposalRequest requestDto = new MentorEditProposalRequest(
			1L,              // jobId
			8,                     // career
			"업데이트 회사",         // currentCompany
			"업데이트된 자기소개"     // introduction
		);

		List<MultipartFile> files = Collections.emptyList(); // 빈 파일 리스트 추가

		// When & Then
		Exception exception = assertThrows(MentorException.class, () -> {
			mentorService.updateMentor(nonExistingMentorId, requestDto, files);
		});

		assertTrue(exception.getMessage().contains("멘토를 찾을 수 없습니다"));
	}

	@Test
	@Order(7)
	@DisplayName("이미 수정 요청 중인 멘토 정보 수정 시 예외 발생")
	void updateMentorFailAlreadyPending() {
		// Given
		Member pendingMember = Member.builder()
			.nickname("pendingMember")
			.password("password")
			.name("수정중회원")
			.email("pending@example.com")
			.userRole(UserRole.MENTOR)
			.build();
		pendingMember = memberRepository.save(pendingMember);

		Mentor pendingMentor = Mentor.builder()
			.member(pendingMember)
			.job(testJob)
			.name(pendingMember.getName())
			.currentCompany("테스트 회사")
			.career(5)
			.phone("01012345678")
			.introduction("테스트 자기소개")
			.modificationStatus(ModificationStatus.PENDING)
			.build();
		pendingMentor = mentorRepository.save(pendingMentor);

		final Long pendingMentorId = pendingMentor.getId();

		MentorEditProposalRequest requestDto = new MentorEditProposalRequest(
			1L,                     // jobId
			8,                      // career
			"업데이트 회사",         // currentCompany
			"업데이트된 자기소개"    // introduction
		);

		List<MultipartFile> files = Collections.emptyList(); // 빈 파일 리스트 추가

		// When & Then
		Exception exception = assertThrows(MentorException.class, () -> {
			mentorService.updateMentor(pendingMentorId, requestDto, files);
		});

		assertTrue(exception.getMessage().contains("이미 정보 수정 요청 중입니다"));
	}

	@Test
	@Order(8)
	@DisplayName("멘토 정보 조회 성공")
	void getMentorInfoSuccess() {
		// Given
		Long mentorId = testMentor.getId();

		// When
		MentorInfoResponse response = mentorService.getMentorInfo(mentorId);

		// Then
		assertNotNull(response, "멘토 정보가 조회되지 않습니다");
		assertEquals(testMentor.getId(), response.memberId());
		assertEquals("테스트멘토", response.name());
		assertEquals("백엔드 개발자", response.jobName());
		assertEquals(5, response.career());
		assertEquals("01012345678", response.phone());
		assertEquals("테스트 회사", response.currentCompany());
		assertEquals("테스트 자기소개", response.introduction());
	}

	@Test
	@Order(9)
	@DisplayName("존재하지 않는 멘토 정보 조회 시 예외 발생")
	void getMentorInfoFailMentorNotFound() {
		// Given
		final Long nonExistingMentorId = 9999L;

		// When & Then
		Exception exception = assertThrows(MentorException.class, () -> {
			mentorService.getMentorInfo(nonExistingMentorId);
		});

		assertTrue(exception.getMessage().contains("해당 멘토를 찾을 수 없습니다"));
	}

	@Test
	@Order(11)
	@DisplayName("멘토 정보 수정 요청 목록 조회 성공")
	void getModificationRequestsSuccess() {
		// Given
		// 수정 요청 생성 및 저장
		MentorEditProposal modification = MentorEditProposal.builder()
			.member(testMentorMember)
			.job(testJob)
			.career(5)
			.currentCompany("업데이트된 회사")
			.introduction("업데이트된 자기소개")
			.status(MentorEditProposalStatus.PENDING)
			.build();
		mentorEditProposalRepository.save(modification);

		// 조회 파라미터 설정
		MentorChangeRequest.ModificationRequestParams params =
			new MentorChangeRequest.ModificationRequestParams(null, 1, 10);

		// When
		MentorChangeResponse.ChangeListResponse response = mentorService.getModificationRequests(
			testMentorMember.getId(), params);

		// Then
		assertNotNull(response, "응답이 null이 아니어야 합니다.");
		assertFalse(response.modificationRequests().isEmpty(), "변경 요청 목록이 비어있지 않아야 합니다.");
		assertEquals(1, response.modificationRequests().size(), "변경 요청 목록의 크기가 1이어야 합니다.");
		assertEquals(MentorEditProposalStatus.PENDING.name(),
			response.modificationRequests().get(0).status(), "상태가 PENDING이어야 합니다.");
		assertNotNull(response.pagination(), "페이지네이션 정보가 null이 아니어야 합니다.");
		assertEquals(1, response.pagination().page(), "현재 페이지가 1이어야 합니다.");
		assertEquals(10, response.pagination().size(), "페이지 크기가 10이어야 합니다.");
		assertEquals(1, response.pagination().totalElements(), "전체 요소 수가 1이어야 합니다.");
	}

	@Test
	@Order(12)
	@DisplayName("멘토 정보 수정 요청 목록 - 상태별 필터링 조회 성공")
	void getModificationRequestsWithStatusFilterSuccess() {
		// Given
		// PENDING 상태의 수정 요청 생성
		MentorEditProposal pendingModification = MentorEditProposal.builder()
			.member(testMentorMember)
			.job(testJob)
			.career(5)
			.currentCompany("업데이트된 회사")
			.introduction("업데이트된 자기소개")
			.status(MentorEditProposalStatus.PENDING)
			.build();
		mentorEditProposalRepository.save(pendingModification);

		// APPROVED 상태의 수정 요청 생성
		MentorEditProposal approvedModification = MentorEditProposal.builder()
			.member(testMentorMember)
			.job(testJob)
			.career(5)
			.currentCompany("업데이트된 회사")
			.introduction("업데이트된 자기소개")
			.status(MentorEditProposalStatus.APPROVED)
			.build();
		mentorEditProposalRepository.save(approvedModification);

		// APPROVED 상태만 필터링하는 파라미터 설정
		MentorChangeRequest.ModificationRequestParams params =
			new MentorChangeRequest.ModificationRequestParams("APPROVED", 1, 10);

		// When
		MentorChangeResponse.ChangeListResponse response = mentorService.getModificationRequests(
			testMentorMember.getId(), params);

		// Then
		assertNotNull(response, "응답이 null이 아니어야 합니다.");
		assertFalse(response.modificationRequests().isEmpty(), "변경 요청 목록이 비어있지 않아야 합니다.");
		assertEquals(1, response.modificationRequests().size(), "변경 요청 목록의 크기가 1이어야 합니다.");
		assertEquals(MentorEditProposalStatus.APPROVED.name(),
			response.modificationRequests().get(0).status(), "상태가 APPROVED이어야 합니다.");
	}

	@Test
	@Order(13)
	@DisplayName("존재하지 않는 멘토의 정보 수정 요청 목록 조회 시 예외 발생")
	void getModificationRequestsFailMentorNotFound() {
		// Given
		final Long nonExistingMentorId = 9999L;
		MentorChangeRequest.ModificationRequestParams params =
			new MentorChangeRequest.ModificationRequestParams(null, 1, 10);

		// When & Then
		Exception exception = assertThrows(MentorException.class, () -> {
			mentorService.getModificationRequests(nonExistingMentorId, params);
		});

		assertTrue(exception.getMessage().contains("해당 멘토를 찾을 수 없습니다"));
	}

	@Test
	@Order(14)
	@DisplayName("수정 요청이 없는 경우 빈 목록 반환")
	void getModificationRequestsWithEmptyList() {
		// Given - 수정 요청을 생성하지 않음
		MentorChangeRequest.ModificationRequestParams params =
			new MentorChangeRequest.ModificationRequestParams(null, 1, 10);

		// When
		MentorChangeResponse.ChangeListResponse response = mentorService.getModificationRequests(
			testMentorMember.getId(), params);

		// Then
		assertNotNull(response, "응답이 null이 아니어야 합니다.");
		assertTrue(response.modificationRequests().isEmpty(), "변경 요청 목록이 비어있어야 합니다.");
		assertEquals(0, response.pagination().totalElements(), "전체 요소 수가 0이어야 합니다.");
	}
}