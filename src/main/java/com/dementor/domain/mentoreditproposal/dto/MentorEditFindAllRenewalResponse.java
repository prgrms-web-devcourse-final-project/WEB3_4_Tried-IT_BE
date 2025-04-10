package com.dementor.domain.mentoreditproposal.dto;

import com.dementor.domain.mentoreditproposal.entity.MentorEditProposal;
import com.dementor.domain.mentoreditproposal.entity.MentorEditProposalStatus;

public record MentorEditFindAllRenewalResponse(
	Long id,
	Long memberId,
	String memberName,
	MentorEditProposalStatus status,
	String createdAt,
	Integer career,
	String currentCompany,
	String introduction
) {
	public static MentorEditFindAllRenewalResponse from(MentorEditProposal mentorEditProposal) {
		return new MentorEditFindAllRenewalResponse(
			mentorEditProposal.getId(),
			mentorEditProposal.getMember().getId(),
			mentorEditProposal.getMember().getName(),
			mentorEditProposal.getStatus(),
			mentorEditProposal.getCreatedAt().toString(),
			mentorEditProposal.getCareer(),
			mentorEditProposal.getCurrentCompany(),
			mentorEditProposal.getIntroduction()
		);
	}
}
