package com.dementor.domain.mentor.dto.response;

import com.dementor.domain.postattachment.entity.PostAttachment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class MentorChangeResponse {
	// 수정 필드 값 DTO (변경 전/후)
	public record FieldChange<T>(
		T before,
		T after
	) {
	}

	// 수정 요청 데이터 개별 항목 DTO
	public record ChangeRequestData(
		Long proposalId,
		String status,
		LocalDateTime createdAt,
		Map<String, FieldChange<?>> modifiedFields,
		List<AttachmentInfo> attachments
	) {
	}

	// 페이지네이션 정보 DTO
	public record Pagination(
		Integer page,
		Integer size,
		Long totalElements
	) {
	}

	// 응답 데이터 DTO
	public record ChangeListResponse(
		List<ChangeRequestData> modificationRequests,
		Pagination pagination
	) {
	}

	public record AttachmentInfo(Long attachmentId, String fileName, String fileUrl) {
		public static AttachmentInfo from(PostAttachment attachment) {
			return new AttachmentInfo(
					attachment.getId(),
					attachment.getFilename(),
					"/api/files/" + attachment.getId() + "/download"
			);
		}
	}
}
