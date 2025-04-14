package com.dementor.domain.mentorapplyproposal.dto.response;

import com.dementor.domain.postattachment.entity.PostAttachment;

public record AttachmentInfo(
	Long attachmentId,
	String fileName,
	String fileUrl
) {
	public static AttachmentInfo from(PostAttachment attachment) {
		return new AttachmentInfo(
				attachment.getId(),
				attachment.getFilename(),
				"/api/files/" + attachment.getId() + "/download"
		);
	}
}
