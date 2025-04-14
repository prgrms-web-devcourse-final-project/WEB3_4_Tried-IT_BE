package com.dementor.domain.mentorapplyproposal.dto.response;

import com.dementor.domain.mentorapplyproposal.entity.MentorApplyProposal;

public record ApplymentResponse(
	Long id,
	Long memberId,
	String name,
	String email,
	String jobName,
	Integer career,
	String status,
	String createdAt
) {
	public static ApplymentResponse from(MentorApplyProposal applyment) {
		return new ApplymentResponse(
			applyment.getId(),
			applyment.getMember().getId(),
			applyment.getName(),
			applyment.getEmail(),
			applyment.getJob().getName(),
			applyment.getCareer(),
			applyment.getStatus().name(),
			applyment.getCreatedAt().toString()
		);
	}
}
