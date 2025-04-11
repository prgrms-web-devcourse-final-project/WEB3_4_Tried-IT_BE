package com.dementor.domain.mentorapplyproposal.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ApplymentRejectRequest(
	@NotBlank(message = "거절 사유는 필수입니다")
	String reason
) {
}
