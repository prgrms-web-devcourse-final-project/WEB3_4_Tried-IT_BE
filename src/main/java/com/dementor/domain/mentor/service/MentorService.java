package com.dementor.domain.mentor.service;

import com.dementor.domain.apply.entity.Apply;
import com.dementor.domain.apply.entity.ApplyStatus;
import com.dementor.domain.apply.repository.ApplyRepository;
import com.dementor.domain.job.entity.Job;
import com.dementor.domain.job.repository.JobRepository;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.entity.UserRole;
import com.dementor.domain.member.repository.MemberRepository;
import com.dementor.domain.mentor.dto.request.MentorApplyProposalRequest;
import com.dementor.domain.mentor.dto.request.MentorApplyStatusRequest;
import com.dementor.domain.mentor.dto.request.MentorChangeRequest;
import com.dementor.domain.mentor.dto.response.MentorApplyResponse;
import com.dementor.domain.mentor.dto.response.MentorApplyStatusResponse;
import com.dementor.domain.mentor.dto.response.MentorChangeResponse;
import com.dementor.domain.mentor.dto.response.MentorInfoResponse;
import com.dementor.domain.mentor.entity.Mentor;
import com.dementor.domain.mentor.entity.ModificationStatus;
import com.dementor.domain.mentor.exception.MentorErrorCode;
import com.dementor.domain.mentor.exception.MentorException;
import com.dementor.domain.mentor.repository.MentorRepository;
import com.dementor.domain.mentorapplyproposal.dto.response.ApplymentResponse;
import com.dementor.domain.mentorapplyproposal.entity.MentorApplyProposal;
import com.dementor.domain.mentorapplyproposal.entity.MentorApplyProposalStatus;
import com.dementor.domain.mentorapplyproposal.repository.MentorApplyProposalRepository;
import com.dementor.domain.mentoreditproposal.dto.MentorEditProposalRequest;
import com.dementor.domain.mentoreditproposal.dto.MentorEditUpdateRenewalResponse;
import com.dementor.domain.mentoreditproposal.entity.MentorEditProposal;
import com.dementor.domain.mentoreditproposal.entity.MentorEditProposalStatus;
import com.dementor.domain.mentoreditproposal.repository.MentorEditProposalRepository;
import com.dementor.domain.postattachment.service.PostAttachmentService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorService {
	private final MentorRepository mentorRepository;
	private final MemberRepository memberRepository;
	private final JobRepository jobRepository;
	private final MentorEditProposalRepository mentorEditProposalRepository;
	private final MentorApplyProposalRepository mentorApplyProposalRepository;
	private final ApplyRepository applyRepository;
	private final PostAttachmentService postAttachmentService;

	//멘토 지원하기
	@Transactional
	public ApplymentResponse applyMentor(
			MentorApplyProposalRequest.MentorApplyProposalRequestDto requestDto,
			List<MultipartFile> files) {

		// 파일이나 마크다운 중 하나는 필수
		validateFileOrMarkdown(files, requestDto.introduction());

		// 회원 엔티티 조회
		Member member = findMemberById(requestDto.memberId());

		// 회원의 역할이 이미 MENTOR인지 확인
		if (member.getUserRole() == UserRole.MENTOR) {
			throw new MentorException(MentorErrorCode.MENTOR_ALREADY_EXISTS,
				"이미 멘토로 등록된 사용자입니다: " + requestDto.memberId());
		}

		// 이미 지원 내역이 있는지 확인
		if (mentorApplyProposalRepository.existsByMemberId(requestDto.memberId())) {
			throw new MentorException(MentorErrorCode.INVALID_MENTOR_APPLICATION,
				"이미 멘토 지원 내역이 존재합니다: " + requestDto.memberId());
		}

		// 직무 엔티티 조회
		Job job = findJobById(requestDto.jobId());

		// 멘토 애플리케이션 엔티티 생성 및 저장
		MentorApplyProposal savedProposal = createAndSaveMentorProposal(requestDto, member, job);

		// 파일 업로드 처리
		if (files != null && !files.isEmpty()) {
			postAttachmentService.uploadFilesApply(files, savedProposal);
		}

		// 응답 데이터 구성
		return ApplymentResponse.from(savedProposal, savedProposal.getJob());
	}

	//멘토 정보 업데이트
	@Transactional
	public MentorEditUpdateRenewalResponse updateMentor(
			Long memberId,
			MentorEditProposalRequest requestDto,
			List<MultipartFile> files) {

		// 파일이나 마크다운 중 하나는 필수
		validateFileOrMarkdown(files, requestDto.getIntroduction());

		Mentor mentor = findMentorById(memberId, "멘토 정보를 업데이트할 수 없습니다: " + memberId);

		// 현재 정보 수정 요청 중인지 확인
		if (mentor.getModificationStatus() == ModificationStatus.PENDING) {
			throw new MentorException(MentorErrorCode.INVALID_MENTOR_APPLICATION,
				"이미 정보 수정 요청 중입니다: " + memberId);
		}

		// 변경 사항이 있는지 확인
		if (!requestDto.hasChanges(mentor)) {
			throw new MentorException(MentorErrorCode.INVALID_MENTOR_APPLICATION,
				"변경된 내용이 없습니다.");
		}

		// 직무 엔티티 조회
		Job job = findJobById(requestDto.getJobId());

		// 수정 요청 엔티티 생성 및 저장
		MentorEditProposal savedModification = createAndSaveMentorModification(requestDto, mentor, job);

		// 멘토의 수정 상태 업데이트
		mentor.updateModificationStatus(ModificationStatus.PENDING);
		mentorRepository.save(mentor);

		// 파일 업로드 처리
		if (files != null && !files.isEmpty()) {
			postAttachmentService.uploadFilesEdit(files, savedModification);
		}

		// 응답 데이터 구성
		return MentorEditUpdateRenewalResponse.from(savedModification);
	}

	//멘토 정보 조회
	@Transactional(readOnly = true)
	public MentorInfoResponse getMentorInfo(Long memberId) {
		Mentor mentor = findMentorById(memberId, "멘토 정보를 조회할 수 없습니다: " + memberId);

		// 멘토의 클래스 ID 목록 조회
		List<Long> classIds = mentorRepository.findMentoringClassIdsByMentor(mentor);

		// 멘토링 클래스 통계 계산
		Integer totalClasses = classIds.size();

		// 대기 중인 요청 수 계산
		Integer pendingRequests = applyRepository.countByMentoringClassIdInAndApplyStatus(
			classIds, ApplyStatus.PENDING);

		// 완료된 멘토링 수 계산 - 멘토링 신청 날짜가 오늘보다 이전이면 완료된 상태
		Integer completedSessions = applyRepository.countCompletedSessions(classIds);

		return MentorInfoResponse.from(mentor, totalClasses, pendingRequests, completedSessions);
	}

	//멘토 정보 수정 요청 목록 조회
	@Transactional(readOnly = true)
	public MentorChangeResponse.ChangeListResponse getModificationRequests(
		Long memberId,
		MentorChangeRequest.ModificationRequestParams params) {

		// 멘토 존재 여부 확인
		checkMentorExists(memberId);

		// 페이지네이션 파라미터 검증
		if (params.page() < 1 || params.size() < 1) {
			throw new MentorException(MentorErrorCode.INVALID_PAGE_PARAMS,
					"페이지 번호와 크기는 1 이상이어야 합니다.");
		}

		// 페이지네이션 설정
		Pageable pageable = PageRequest.of(
			params.page() - 1, // 0-based page index
			params.size(),
			Sort.by(Sort.Direction.DESC, "createdAt")
		);

		try {
			// 상태 필터가 있으면 상태별로 조회, 없으면 전체 조회
			Page<MentorEditProposal> modificationPage;

			if (params.status() != null) {
				MentorEditProposalStatus status = MentorEditProposalStatus.valueOf(params.status());
				modificationPage = mentorEditProposalRepository.findByMemberIdAndStatus(memberId, status, pageable);
			} else {
				modificationPage = mentorEditProposalRepository.findByMemberId(memberId, pageable);
			}

			// 결과 변환 및 반환
			List<MentorChangeResponse.ChangeRequestData> changeRequests = modificationPage.getContent().stream()
				.map(this::convertToChangeRequestData)
				.collect(Collectors.toList());

			return new MentorChangeResponse.ChangeListResponse(
				changeRequests,
				new MentorChangeResponse.Pagination(
					params.page(),
					params.size(),
					modificationPage.getTotalElements()
				)
			);
		} catch (IllegalArgumentException e) {
			throw new MentorException(MentorErrorCode.INVALID_STATUS_PARAM,
					"유효하지 않은 상태값입니다: " + params.status());
		} catch (Exception e) {
			throw new MentorException(MentorErrorCode.INTERNAL_SERVER_ERROR,
					"멘토 정보 수정 요청 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	@Transactional(readOnly = true)
	public MentorApplyResponse.GetApplyMenteePageList getApplyByMentor(Long memberId, int page, int size) {

		Mentor mentor = findMentorById(memberId, "멘토만 조회할 수 있습니다.");

		// 멘토가 가진 클래스 아이디 목록 조회
		List<Long> classId = mentorRepository.findMentoringClassIdsByMentor(mentor);

		// 멘토가 가진 클래스 아이디 목록으로 신청 목록 조회
		Page<Apply> applyPage = applyRepository.findByMentoringClassIdIn(classId, PageRequest.of(page, size));

		return MentorApplyResponse.GetApplyMenteePageList.from(applyPage, page, size);

	}

	@Transactional
	public MentorApplyStatusResponse updateApplyStatus(Long memberId, Long applyId, MentorApplyStatusRequest request) {
		Mentor mentor = findMentorById(memberId, "멘토만 상태를 변경할 수 있습니다.");

		// 신청 정보 조회
		Apply apply = applyRepository.findById(applyId)
				.orElseThrow(() -> new MentorException(MentorErrorCode.INVALID_MENTOR_APPLICATION,
						"존재하지 않는 신청입니다."));

		// 멘토의 클래스인지 확인
		List<Long> classIds = mentorRepository.findMentoringClassIdsByMentor(mentor);
		if (!classIds.contains(apply.getMentoringClass().getId())) {
			throw new AccessDeniedException("자신의 멘토링 클래스에 대한 신청만 변경할 수 있습니다.");
		}

		// 이미 승인/거절을 한 신청인지 확인
		if (apply.getApplyStatus() == ApplyStatus.APPROVED || apply.getApplyStatus() == ApplyStatus.REJECTED) {
			throw new MentorException(MentorErrorCode.INVALID_MENTOR_APPLICATION, "이미 승인/거절된 신청입니다.");
		}

		// 상태 변경
		ApplyStatus newStatus = request.getStatus();
		apply.updateStatus(newStatus);

		// 변경된 엔티티 저장
		Apply updatedApply = applyRepository.save(apply);

		return MentorApplyStatusResponse.from(updatedApply);
	}

	// 멘토 지원서 생성 및 저장을 위한 내부 메소드
	private MentorApplyProposal createAndSaveMentorProposal(
			MentorApplyProposalRequest.MentorApplyProposalRequestDto requestDto,
			Member member,
			Job job) {

		MentorApplyProposal mentorApplyProposal = MentorApplyProposal.builder()
				.member(member)
				.job(job)
				.name(requestDto.name())
				.career(requestDto.career())
				.phone(requestDto.phone())
				.email(requestDto.email())
				.currentCompany(requestDto.currentCompany())
				.introduction(requestDto.introduction())
				.status(MentorApplyProposalStatus.PENDING)
				.build();

		return mentorApplyProposalRepository.save(mentorApplyProposal);
	}

	// 멘토 수정 요청 생성 및 저장을 위한 내부 메소드
	private MentorEditProposal createAndSaveMentorModification(
			MentorEditProposalRequest requestDto,
			Mentor mentor,
			Job job) {

		MentorEditProposal modification = MentorEditProposal.builder()
				.member(mentor.getMember())
				.career(requestDto.getCareer())
				.currentCompany(requestDto.getCurrentCompany())
				.job(job)
				.introduction(requestDto.getIntroduction())
				.status(MentorEditProposalStatus.PENDING)
				.build();

		return mentorEditProposalRepository.save(modification);
	}

	//멘토 정보 수정 요청을 DTO로 변환합니다.
	private MentorChangeResponse.ChangeRequestData convertToChangeRequestData(MentorEditProposal proposal) {
		Map<String, MentorChangeResponse.FieldChange<?>> modifiedFields = new HashMap<>();

		// 기존 멘토 정보 조회
		Mentor mentor = findMentorById(proposal.getMember().getId(), "멘토의 변경 내역을 조회할 수 없습니다: " + proposal.getMember().getId());

		if (!proposal.getCareer().equals(mentor.getCareer())) {
			modifiedFields.put("career",
					new MentorChangeResponse.FieldChange<>(mentor.getCareer(), proposal.getCareer()));
		}
		if (!proposal.getCurrentCompany().equals(mentor.getCurrentCompany())) {
			modifiedFields.put("currentCompany",
					new MentorChangeResponse.FieldChange<>(mentor.getCurrentCompany(), proposal.getCurrentCompany()));
		}
		if (!proposal.getJob().getId().equals(mentor.getJob().getId())) {
			modifiedFields.put("jobId",
					new MentorChangeResponse.FieldChange<>(mentor.getJob().getId(), proposal.getJob().getId()));
		}
		if (!proposal.getIntroduction().equals(mentor.getIntroduction())) {
			modifiedFields.put("introduction",
					new MentorChangeResponse.FieldChange<>(mentor.getIntroduction(), proposal.getIntroduction()));
		}

		return new MentorChangeResponse.ChangeRequestData(
				proposal.getId(),
				proposal.getStatus().name(),
				proposal.getCreatedAt(),
				modifiedFields
		);
	}

	// 멤버 ID로 멤버 조회
	private Member findMemberById(Long memberId) {
		return memberRepository.findById(memberId)
				.orElseThrow(() -> new MentorException(MentorErrorCode.MENTOR_NOT_FOUND,
						"회원을 찾을 수 없습니다: " + memberId));
	}

	// 직무 ID로 직무 조회
	private Job findJobById(Long jobId) {
		return jobRepository.findById(jobId)
				.orElseThrow(() -> new MentorException(MentorErrorCode.JOB_NOT_FOUND,
						"직무를 찾을 수 없습니다: " + jobId));
	}

	// 멘토 ID로 멘토 조회 (사용자 정의 오류 메시지 지원)
	private Mentor findMentorById(Long mentorId, String errorMessage) {
		return mentorRepository.findById(mentorId)
				.orElseThrow(() -> new MentorException(MentorErrorCode.MENTOR_NOT_FOUND, errorMessage));
	}

	// 멘토 존재 여부 확인
	private void checkMentorExists(Long mentorId) {
		if (!mentorRepository.existsById(mentorId)) {
			throw new MentorException(MentorErrorCode.MENTOR_NOT_FOUND,
					"해당 멘토를 찾을 수 없습니다: " + mentorId);
		}
	}

	// 파일 또는 마크다운 필수 검증
	private void validateFileOrMarkdown(List<MultipartFile> files, String markdown) {
		if ((files == null || files.isEmpty()) && (markdown == null || markdown.isEmpty())) {
			throw new MentorException(MentorErrorCode.INVALID_MENTOR_APPLICATION,
					"파일 또는 마크다운 텍스트 중 하나는 필수입니다.");
		}
	}
}