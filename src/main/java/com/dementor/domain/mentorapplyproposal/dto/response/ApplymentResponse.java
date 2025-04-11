package com.dementor.domain.mentorapplyproposal.dto.response;

import com.dementor.domain.job.entity.Job;
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
	public static ApplymentResponse from(MentorApplyProposal applyment, Job job) {
		return new ApplymentResponse(
			applyment.getId(),
			applyment.getMember().getId(),
			applyment.getName(),
			applyment.getEmail(),
			job.getName(),
			applyment.getCareer(),
			applyment.getStatus().name(),
			applyment.getCreatedAt().toString()
		);
	}
}
