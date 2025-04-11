package com.dementor.domain.mentorapplyproposal.dto.response;

import com.dementor.domain.mentor.entity.Mentor;
import com.dementor.domain.mentorapplyproposal.entity.MentorApplyProposal;

public record ApplymentApprovalResponse(
	Long id,
	Long memberId,
	Long mentorId,
	String name,
	String status,
	String modifiedAt
) {
	public static ApplymentApprovalResponse from(MentorApplyProposal applyment, Mentor mentor) {
		return new ApplymentApprovalResponse(
			applyment.getId(),
			applyment.getMember().getId(),
			mentor.getId(),
			applyment.getName(),
			applyment.getStatus().name(),
			applyment.getModifiedAt().toString()
		);
	}
}
