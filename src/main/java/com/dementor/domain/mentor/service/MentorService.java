package com.dementor.domain.mentor.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

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
import com.dementor.domain.mentorapplyproposal.entity.MentorApplyProposal;
import com.dementor.domain.mentorapplyproposal.entity.MentorApplyProposalStatus;
import com.dementor.domain.mentorapplyproposal.repository.MentorApplyProposalRepository;
import com.dementor.domain.mentoreditproposal.dto.MentorEditProposalRequest;
import com.dementor.domain.mentoreditproposal.dto.MentorUpdateRequest;
import com.dementor.domain.mentoreditproposal.entity.MentorEditProposal;
import com.dementor.domain.mentoreditproposal.entity.MentorEditProposalStatus;
import com.dementor.domain.mentoreditproposal.repository.MentorEditProposalRepository;
import com.dementor.domain.postattachment.repository.PostAttachmentRepository;
import com.dementor.domain.postattachment.service.PostAttachmentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MentorService {
	private final MentorRepository mentorRepository;
	private final MemberRepository memberRepository;
	private final JobRepository jobRepository;
	private final PostAttachmentRepository attachmentRepository;
	private final MentorEditProposalRepository mentorEditProposalRepository;
	private final MentorApplyProposalRepository mentorApplyProposalRepository;
	private final ObjectMapper objectMapper;
	private final PostAttachmentService postAttachmentService;
	private final ApplyRepository applyRepository;

	//멘토 지원하기
	@Transactional
	public void applyMentor(MentorApplyProposalRequest.MentorApplyProposalRequestDto requestDto) {
		// 회원 엔티티 조회
		Member member = memberRepository.findById(requestDto.memberId())
			.orElseThrow(() -> new MentorException(MentorErrorCode.MENTOR_NOT_FOUND,
				"회원을 찾을 수 없습니다: " + requestDto.memberId()));

		// 회원의 역할이 이미 MENTOR인지 확인
		if (member.getUserRole() == UserRole.MENTOR) {
			throw new MentorException(MentorErrorCode.MENTOR_ALREADY_EXISTS,
				"이미 멘토로 등록된 사용자입니다: " + requestDto.memberId());
		}

		// 직무 엔티티 조회
		Job job = jobRepository.findById(requestDto.jobId())
			.orElseThrow(() -> new MentorException(MentorErrorCode.INVALID_MENTOR_APPLICATION,
				"직무를 찾을 수 없습니다: " + requestDto.jobId()));

		// 이미 지원 내역이 있는지 확인
		if (mentorApplyProposalRepository.existsByMemberId(requestDto.memberId())) {
			throw new MentorException(MentorErrorCode.INVALID_MENTOR_APPLICATION,
				"이미 멘토 지원 내역이 존재합니다: " + requestDto.memberId());
		}

		// 멘토 애플리케이션 엔티티 생성 - 초기 상태는 PENDING
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

		// 멘토 애플리케이션 저장 (ID 생성)
		MentorApplyProposal savedApplication = mentorApplyProposalRepository.save(mentorApplyProposal);

		// 첨부파일 연결
		if (requestDto.attachmentId() != null && !requestDto.attachmentId().isEmpty()) {
			for (Long attachmentId : requestDto.attachmentId()) {
				attachmentRepository.findById(attachmentId)
					.ifPresent(attachment -> {
						if (!attachment.getMember().getId().equals(member.getId())) {
							throw new MentorException(MentorErrorCode.UNAUTHORIZED_ACCESS,
								"본인이 업로드한 파일만 연결할 수 있습니다: " + attachmentId);
						}
						attachment.connectToMentorApplyProposal(savedApplication);
						attachmentRepository.save(attachment);
					});
			}
		}
	}

	//TODO: 꼭 삭제해야함 테스트 오류때문에 만들어놓음
	@Transactional
	public void updateMentor(Long memberId, MentorUpdateRequest.MentorUpdateRequestDto requestDto) {
		Mentor mentor = mentorRepository.findById(memberId)
			.orElseThrow(() -> new MentorException(MentorErrorCode.MENTOR_NOT_FOUND,
				"멘토를 찾을 수 없습니다: " + memberId));


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

		// 변경 사항 추출
		Map<String, Map<String, Object>> changes = extractChanges(mentor, requestDto);

		// 변경 사항을 JSON으로 변환
		String changesJson;
		try {
			changesJson = objectMapper.writeValueAsString(changes);
		} catch (JsonProcessingException e) {
			throw new MentorException(MentorErrorCode.INVALID_MENTOR_APPLICATION,
				"변경 내용을 JSON으로 변환하는데 실패했습니다: " + e.getMessage());
		}

		// 수정 요청 엔티티 생성 및 저장
		MentorEditProposal modification = MentorEditProposal.builder()
			.member(mentor.getMember())
			.status(MentorEditProposalStatus.PENDING)
			.build();

		MentorEditProposal savedModification = mentorEditProposalRepository.save(modification);

		// 첨부 파일 처리
		if (requestDto.attachmentId() != null && !requestDto.attachmentId().isEmpty()) {
			for (Long attachmentId : requestDto.attachmentId()) {
				// 첨부 파일 존재 여부 확인
				attachmentRepository.findById(attachmentId)
					.ifPresent(attachment -> {
						if (!attachment.getMember().getId().equals(mentor.getMember().getId())) {
							throw new MentorException(MentorErrorCode.UNAUTHORIZED_ACCESS,
								"본인이 업로드한 파일만 연결할 수 있습니다: " + attachmentId);
						}
						attachment.connectToMentorModification(savedModification);
						attachmentRepository.save(attachment);
					});
			}
		}

		// 멘토의 수정 상태 업데이트
		mentor.updateModificationStatus(ModificationStatus.PENDING);
		mentorRepository.save(mentor);
	}



	//멘토 정보 업데이트
	@Transactional
	public void updateMentor(Long memberId, MentorEditProposalRequest requestDto) {
		Mentor mentor = mentorRepository.findById(memberId)
			.orElseThrow(() -> new MentorException(MentorErrorCode.MENTOR_NOT_FOUND,
				"멘토를 찾을 수 없습니다: " + memberId));

		// 현재 정보 수정 요청 중인지 확인
		if (mentor.getModificationStatus() == ModificationStatus.PENDING) {
			throw new MentorException(MentorErrorCode.INVALID_MENTOR_APPLICATION,
				"이미 정보 수정 요청 중입니다: " + memberId);
		}

		// 변경 사항 추출
		//Map<String, Map<String, Object>> changes = extractChanges(mentor, requestDto);

		// 변경 사항을 JSON으로 변환
		// String changesJson;
		// try {
		//     changesJson = objectMapper.writeValueAsString(changes);
		// } catch (JsonProcessingException e) {
		//     throw new MentorException(MentorErrorCode.INVALID_MENTOR_APPLICATION,
		//             "변경 내용을 JSON으로 변환하는데 실패했습니다: " + e.getMessage());
		// }
		Job job = jobRepository.findById(requestDto.getJobId())
			.orElseThrow(() -> new RuntimeException("job not found"));

		// 수정 요청 엔티티 생성 및 저장
		MentorEditProposal modification = MentorEditProposal.builder()
			.member(mentor.getMember())
			.job(job)
			.career(requestDto.getCareer())
			.currentCompany(requestDto.getCurrentCompany())
			.introduction(requestDto.getIntroduction())
			.status(MentorEditProposalStatus.PENDING)
			.build();

		MentorEditProposal mentorEditProposalSaved = mentorEditProposalRepository.save(modification);

		// 첨부 파일 처리
		//PostAttachment attachment = PostAttachment.builder()


		// 멘토의 수정 상태 업데이트
		mentor.updateModificationStatus(ModificationStatus.PENDING);
		mentorRepository.save(mentor);
	}

	//멘토 정보 조회
	public MentorInfoResponse getMentorInfo(Long memberId) {
		Mentor mentor = mentorRepository.findById(memberId)
			.orElseThrow(() -> new MentorException(MentorErrorCode.MENTOR_NOT_FOUND,
				"해당 멘토를 찾을 수 없습니다: " + memberId));

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
	public MentorChangeResponse.ChangeListResponse getModificationRequests(
		Long memberId,
		MentorChangeRequest.ModificationRequestParams params) {
		// 멘토 존재 여부 확인
		if (!mentorRepository.existsById(memberId)) {
			throw new MentorException(MentorErrorCode.MENTOR_NOT_FOUND,
				"해당 멘토를 찾을 수 없습니다: " + memberId);
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
		} catch (Exception e) {
			throw new MentorException(MentorErrorCode.INVALID_STATUS_PARAM,
				"멘토 정보 수정 요청 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	//멘토 정보 수정 요청을 DTO로 변환합니다.
	private MentorChangeResponse.ChangeRequestData convertToChangeRequestData(MentorEditProposal modification) {
		Map<String, MentorChangeResponse.FieldChange<?>> modifiedFields = new HashMap<>();

		try {
			// Map<String, Map<String, Object>> changes = objectMapper.readValue(
			// 	modification.getChanges(),
			// 	new TypeReference<Map<String, Map<String, Object>>>() {
			// 	}
			// );

			// for (Map.Entry<String, Map<String, Object>> entry : changes.entrySet()) {
			// 	String fieldName = entry.getKey();
			// 	Map<String, Object> change = entry.getValue();
			//
			// 	Object before = change.get("before");
			// 	Object after = change.get("after");
			//
			// 	modifiedFields.put(fieldName, new MentorChangeResponse.FieldChange<>(before, after));
			// }
		} catch (Exception e) {
			// JSON 파싱 실패 시 빈 맵 반환
		}

		return new MentorChangeResponse.ChangeRequestData(
			modification.getId(),
			modification.getStatus().name(),
			modification.getCreatedAt(),
			modifiedFields
		);
	}

	//멘토 엔티티와 수정 요청 DTO를 비교하여 변경 사항을 추출합니다.
	private Map<String, Map<String, Object>> extractChanges(Mentor mentor,
		MentorUpdateRequest.MentorUpdateRequestDto dto) {
		Map<String, Map<String, Object>> changes = new HashMap<>();

		// career 변경 확인
		if (dto.career() != null && !dto.career().equals(mentor.getCareer())) {
			Map<String, Object> fieldChange = new HashMap<>();
			fieldChange.put("before", mentor.getCareer());
			fieldChange.put("after", dto.career());
			changes.put("career", fieldChange);
		}

		// phone 변경 확인
		if (dto.phone() != null && !dto.phone().equals(mentor.getPhone())) {
			Map<String, Object> fieldChange = new HashMap<>();
			fieldChange.put("before", mentor.getPhone());
			fieldChange.put("after", dto.phone());
			changes.put("phone", fieldChange);
		}

		// currentCompany 변경 확인
		if (dto.currentCompany() != null && !dto.currentCompany().equals(mentor.getCurrentCompany())) {
			Map<String, Object> fieldChange = new HashMap<>();
			fieldChange.put("before", mentor.getCurrentCompany());
			fieldChange.put("after", dto.currentCompany());
			changes.put("currentCompany", fieldChange);
		}

		// jobId 변경 확인
		if (dto.jobId() != null && !dto.jobId().equals(mentor.getJob().getId())) {
			Map<String, Object> fieldChange = new HashMap<>();
			fieldChange.put("before", mentor.getJob().getId());
			fieldChange.put("after", dto.jobId());
			changes.put("jobId", fieldChange);
		}

		// email 변경 확인 - 이제 멘토 엔티티에서 직접 확인
		if (dto.email() != null && !dto.email().equals(mentor.getEmail())) {
			Map<String, Object> fieldChange = new HashMap<>();
			fieldChange.put("before", mentor.getEmail());
			fieldChange.put("after", dto.email());
			changes.put("email", fieldChange);
		}

		// introduction 변경 확인
		if (dto.introduction() != null && !dto.introduction().equals(mentor.getIntroduction())) {
			Map<String, Object> fieldChange = new HashMap<>();
			fieldChange.put("before", mentor.getIntroduction());
			fieldChange.put("after", dto.introduction());
			changes.put("introduction", fieldChange);
		}

		return changes;
	}

	@Transactional
	public MentorApplyResponse.GetApplyMenteePageList getApplyByMentor(Long memberId, int page, int size) {

		Mentor mentor = mentorRepository.findById(memberId)
			.orElseThrow(() -> new IllegalArgumentException("멘토만 조회할 수 있습니다."));

		// 멘토가 가진 클래스 아이디 목록 조회
		List<Long> classId = mentorRepository.findMentoringClassIdsByMentor(mentor);

		// 멘토가 가진 클래스 아이디 목록으로 신청 목록 조회
		Page<Apply> applyPage = applyRepository.findByMentoringClassIdIn(classId, PageRequest.of(page, size));

		return MentorApplyResponse.GetApplyMenteePageList.from(applyPage, page, size);

	}

	@Transactional
	public MentorApplyStatusResponse updateApplyStatus(Long memberId, Long applyId, MentorApplyStatusRequest request) {
		// 멘토 검증
		Mentor mentor = mentorRepository.findById(memberId)
			.orElseThrow(() -> new IllegalArgumentException("멘토만 상태를 변경할 수 있습니다."));

		// 신청 정보 조회
		Apply apply = applyRepository.findById(applyId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신청입니다."));

		// 멘토의 클래스인지 확인
		List<Long> classIds = mentorRepository.findMentoringClassIdsByMentor(mentor);
		if (!classIds.contains(apply.getMentoringClass().getId())) {
			throw new AccessDeniedException("자신의 멘토링 클래스에 대한 신청만 변경할 수 있습니다.");
		}

		//이미 승인/거절을 한 신청인지 확인
		if (apply.getApplyStatus() == ApplyStatus.APPROVED || apply.getApplyStatus() == ApplyStatus.REJECTED) {
			throw new IllegalArgumentException("이미 승인/거절된 신청입니다.");
		}

		// 상태 변경
		ApplyStatus newStatus = request.getStatus();
		apply.updateStatus(newStatus);

		// 변경된 엔티티 저장
		Apply updatedApply = applyRepository.save(apply);

		return MentorApplyStatusResponse.from(updatedApply);
	}

}