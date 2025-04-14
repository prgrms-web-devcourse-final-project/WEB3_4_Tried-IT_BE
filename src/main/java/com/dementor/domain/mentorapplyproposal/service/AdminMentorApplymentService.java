package com.dementor.domain.mentorapplyproposal.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.dementor.domain.mentorapplyproposal.dto.request.ApplymentRejectRequest;
import com.dementor.domain.mentorapplyproposal.dto.response.ApplymentApprovalResponse;
import com.dementor.domain.mentorapplyproposal.dto.response.ApplymentDetailResponse;
import com.dementor.domain.mentorapplyproposal.dto.response.ApplymentRejectResponse;
import com.dementor.domain.mentorapplyproposal.dto.response.ApplymentResponse;
import com.dementor.domain.job.entity.Job;
import com.dementor.domain.job.repository.JobRepository;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.entity.UserRole;
import com.dementor.domain.member.repository.MemberRepository;
import com.dementor.domain.mentor.entity.Mentor;
import com.dementor.domain.mentor.repository.MentorRepository;
import com.dementor.domain.mentorapplyproposal.entity.MentorApplyProposal;
import com.dementor.domain.mentorapplyproposal.entity.MentorApplyProposalStatus;
import com.dementor.domain.mentorapplyproposal.repository.MentorApplyProposalRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminMentorApplymentService {
	private final JobRepository jobRepository;
	private final MentorRepository mentorRepository;
	private final MemberRepository memberRepository;
	private final MentorApplyProposalRepository mentorApplyProposalRepository;

	public Page<ApplymentResponse> findAllApplyment(Pageable pageable) {
		return mentorApplyProposalRepository.findAll(pageable)
			.map(application -> ApplymentResponse.from(application, application.getJob()));
	}

	public ApplymentDetailResponse findOneApplyment(Long memberId) {
		// 지원서 조회
		MentorApplyProposal applyment = mentorApplyProposalRepository.findByMemberId(memberId)
			.orElseThrow(() -> new EntityNotFoundException("지원서를 찾을 수 없습니다."));

		// 첨부파일 목록 조회
		//        List<PostAttachment> attachments = attachmentRepository.findByApplymentId(memberId);

		return ApplymentDetailResponse.from(
			applyment
			//                job
			//                attachments
		);
	}

	public ApplymentApprovalResponse approveApplyment(Long memberId) {
		// 지원서 조회
		MentorApplyProposal applyment = mentorApplyProposalRepository.findByMemberIdAndStatus(memberId, MentorApplyProposalStatus.PENDING)
			.orElseThrow(() -> new EntityNotFoundException("지원서를 찾을 수 없습니다."));

		// 회원 정보 조회
		Member member = memberRepository.findById(applyment.getMember().getId())
			.orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

		// 직무 정보 조회
		Job job = jobRepository.findById(applyment.getJob().getId())
			.orElseThrow(() -> new EntityNotFoundException("직무 정보를 찾을 수 없습니다."));

		// 멘토 엔티티 생성
		Mentor mentor = Mentor.builder()
			.member(member)
			.job(job)
			.name(applyment.getName())
			.currentCompany(applyment.getCurrentCompany())
			.career(applyment.getCareer())
			.phone(applyment.getPhone())
			.email(applyment.getEmail())
			.introduction(applyment.getIntroduction())
			.build();
		mentorRepository.save(mentor);
		member.updateUserRole(UserRole.MENTOR);
		memberRepository.save(member);

		applyment.updateStatus(MentorApplyProposalStatus.APPROVED);
		MentorApplyProposal updatedApplyment = mentorApplyProposalRepository.save(applyment);

		return ApplymentApprovalResponse.from(updatedApplyment, mentor);
	}

	public ApplymentRejectResponse rejectApplyment(Long memberId, ApplymentRejectRequest request) {
		// 지원서 조회
		MentorApplyProposal applyment = mentorApplyProposalRepository.findByMemberIdAndStatus(memberId, MentorApplyProposalStatus.PENDING)
			.orElseThrow(() -> new EntityNotFoundException("지원서를 찾을 수 없습니다."));

		// 지원서 상태를 거절로 변경하고 거절 사유 저장
		applyment.updateStatus(MentorApplyProposalStatus.REJECTED);
		// 저장
		MentorApplyProposal updatedApplyment = mentorApplyProposalRepository.save(applyment);

		// 회원 정보 조회
		Member member = memberRepository.findById(applyment.getMember().getId())
			.orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

		// Response 생성
		return ApplymentRejectResponse.of(
			updatedApplyment,
			member,
			request.reason()
		);
	}
}
