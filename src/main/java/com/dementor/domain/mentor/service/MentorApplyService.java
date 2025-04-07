package com.dementor.domain.mentor.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dementor.domain.apply.entity.Apply;
import com.dementor.domain.apply.entity.ApplyStatus;
import com.dementor.domain.apply.repository.ApplyRepository;
import com.dementor.domain.mentor.dto.request.MentorApplyStatusRequest;
import com.dementor.domain.mentor.dto.response.MentorApplyStatusResponse;
import com.dementor.domain.mentor.dto.response.MentorApplyResponse;
import com.dementor.domain.mentor.entity.Mentor;
import com.dementor.domain.mentor.repository.MentorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MentorApplyService {

	private final ApplyRepository applyRepository;
	private final MentorRepository mentorRepository;


	public MentorApplyResponse.GetApplyMenteePageList getApplyByMentor(Long memberId, int page, int size) {

		Mentor mentor = mentorRepository.findById(memberId)
			.orElseThrow(() -> new IllegalArgumentException("멘토만 조회할 수 있습니다."));


		if (mentor.getApprovalStatus() != Mentor.ApprovalStatus.APPROVED) {
			throw new AccessDeniedException("승인되지 않은 멘토는 신청 목록을 조회할 수 없습니다");
		}

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
			
		if (mentor.getApprovalStatus() != Mentor.ApprovalStatus.APPROVED) {
			throw new AccessDeniedException("승인되지 않은 멘토는 신청 상태를 변경할 수 없습니다");
		}
		
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
