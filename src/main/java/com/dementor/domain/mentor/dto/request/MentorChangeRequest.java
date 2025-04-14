package com.dementor.domain.mentor.dto.request;

import jakarta.validation.constraints.Min;

public class MentorChangeRequest {
	// 요청 파라미터를 위한 DTO
	public record ModificationRequestParams(
		String status,
		@Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
		Integer page,

		@Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
		Integer size
	) {
	}
}
