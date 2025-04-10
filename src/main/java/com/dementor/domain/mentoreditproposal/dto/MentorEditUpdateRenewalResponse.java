package com.dementor.domain.mentoreditproposal.dto;

import com.dementor.domain.mentoreditproposal.entity.MentorEditProposal;
import com.dementor.domain.mentoreditproposal.entity.MentorEditProposalStatus;

public record MentorEditUpdateRenewalResponse(

	Long id,
	Long memberId,
	MentorEditProposalStatus status,
	String modifiedAt
) {
	public static MentorEditUpdateRenewalResponse from(MentorEditProposal mentorEditProposal) {
		return new MentorEditUpdateRenewalResponse(
			mentorEditProposal.getId(),
			mentorEditProposal.getMember().getId(),
			mentorEditProposal.getStatus(),
			mentorEditProposal.getModifiedAt().toString()
		);
	}
}
