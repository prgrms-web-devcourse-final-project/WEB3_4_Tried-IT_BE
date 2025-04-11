package com.dementor.domain.mentor.dto.response;

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
		Long requestId,
		String status,
		LocalDateTime requestDate,
		Map<String, FieldChange<?>> modifiedFields
	) {
	}

	// 페이지네이션 정보 DTO
	public record Pagination(
		Integer page,
		Integer size,
		Long totalElements
	) {
		public Long getTotalPages() {
			return (totalElements + size - 1) / size;
		}
	}

	// 응답 데이터 DTO
	public record ChangeListResponse(
		List<ChangeRequestData> modificationRequests,
		Pagination pagination
	) {
	}
}
