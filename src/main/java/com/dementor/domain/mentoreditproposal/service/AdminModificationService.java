package com.dementor.domain.mentoreditproposal.service;

import com.dementor.domain.mentor.entity.Mentor;
import com.dementor.domain.mentor.entity.ModificationStatus;
import com.dementor.domain.mentor.repository.MentorRepository;
import com.dementor.domain.mentoreditproposal.dto.MentorEditFindAllRenewalResponse;
import com.dementor.domain.mentoreditproposal.dto.MentorEditUpdateRenewalResponse;
import com.dementor.domain.mentoreditproposal.entity.MentorEditProposal;
import com.dementor.domain.mentoreditproposal.entity.MentorEditProposalStatus;
import com.dementor.domain.mentoreditproposal.repository.AdminModificationRepository;
import com.dementor.domain.mentoreditproposal.repository.MentorEditProposalRepository;
import com.dementor.domain.postattachment.repository.PostAttachmentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminModificationService {
	private final AdminModificationRepository adminModificationRepository;
	private final MentorEditProposalRepository mentorEditProposalRepository;
	private final MentorRepository mentorRepository;
	private final PostAttachmentRepository postAttachmentRepository;

	public Page<MentorEditFindAllRenewalResponse> findAllModificationRequest(Pageable pageable) {
		Page<MentorEditProposal> mentorEditProposalPage;

		// TODO : Pending 상태인것만 찾고 싶으면 추가 조건문 작성 필요
		mentorEditProposalPage = adminModificationRepository.findAll(pageable);

		return mentorEditProposalPage.map(MentorEditFindAllRenewalResponse::from);
	}

	public MentorEditUpdateRenewalResponse approveMentorUpdate(Long memberId) {
		MentorEditProposal mentorEditProposal = mentorEditProposalRepository.findOneRequestByMemberIdAndStatus(memberId,
			MentorEditProposalStatus.PENDING);

		Mentor mentor = mentorRepository.findByMemberId(memberId)
			.orElseThrow(() -> new EntityNotFoundException("Mentor not found"));

		mentor.update(
			mentorEditProposal.getCurrentCompany(),
			mentorEditProposal.getCareer(),
			mentorEditProposal.getJob(),
			mentorEditProposal.getIntroduction(),
			ModificationStatus.APPROVED
		);

		mentorEditProposal.updateStatus(MentorEditProposalStatus.APPROVED);
		mentorEditProposalRepository.save(mentorEditProposal);
		mentorRepository.save(mentor);

		return MentorEditUpdateRenewalResponse.from(mentorEditProposal);
	}

	@Transactional
	public MentorEditUpdateRenewalResponse rejectMentorUpdate(Long memberId) {
		MentorEditProposal mentorEditProposal = mentorEditProposalRepository.findOneRequestByMemberIdAndStatus(memberId,
			MentorEditProposalStatus.PENDING);

		Mentor mentor = mentorRepository.findByMemberId(memberId)
			.orElseThrow(() -> new EntityNotFoundException("Mentor not found"));

		mentor.updateModificationStatus(ModificationStatus.REJECTED);

		mentorEditProposal.updateStatus(MentorEditProposalStatus.REJECTED);
		mentorEditProposalRepository.save(mentorEditProposal);
		mentorRepository.save(mentor);

		return MentorEditUpdateRenewalResponse.from(mentorEditProposal);
	}
}
