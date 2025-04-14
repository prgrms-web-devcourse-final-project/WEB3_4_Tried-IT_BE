package com.dementor.domain.mentorapplyproposal.dto.response;

import com.dementor.domain.mentorapplyproposal.entity.MentorApplyProposal;
import com.dementor.domain.postattachment.entity.PostAttachment;

import java.util.List;

public record ApplymentInfo(
	Long applymentId,
	Long memberId,
	String name,
	JobInfo job,
	Integer career,
	String phone,
	String email,
	String currentCompany,
	String introduction,
	String status,
	String createdAt,
	String modifiedAt,
	List<AttachmentInfo> attachments
) {
	public static ApplymentInfo from(
		MentorApplyProposal applyment,
		List<PostAttachment> attachments
	) {
		return new ApplymentInfo(
			applyment.getId(),
			applyment.getMember().getId(),
			applyment.getName(),
			JobInfo.from(applyment.getJob()),
			applyment.getCareer(),
			applyment.getPhone(),
			applyment.getEmail(),
			applyment.getCurrentCompany(),
			applyment.getIntroduction(),
			applyment.getStatus().name(),
			applyment.getCreatedAt().toString(),
			applyment.getModifiedAt() != null ? applyment.getModifiedAt().toString() : null,
			attachments.stream()
					.map(AttachmentInfo::from)
					.toList()
		);
	}
}
