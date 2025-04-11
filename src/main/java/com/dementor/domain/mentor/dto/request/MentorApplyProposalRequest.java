package com.dementor.domain.mentor.dto.request;

import jakarta.validation.constraints.*;

import java.util.List;

public class MentorApplyProposalRequest {
	public record MentorApplyProposalRequestDto(
		@NotNull(message = "회원 ID는 필수 입력 항목입니다.")
		Long memberId,

		@NotBlank(message = "이름은 필수입니다.")
		@Size(max = 10, message = "이름은 10자 이내로 입력해주세요.")
		String name,

		@NotNull(message = "직무 ID는 필수 입력 항목입니다.")
		Long jobId,

		@NotBlank(message = "전화번호는 필수 입력 항목입니다.")
		@Size(max = 20, message = "전화번호는 20자 이내로 입력해주세요.")
		@Pattern(regexp = "^[\\d-]{10,13}$", message = "전화번호는 10~11자리 숫자와 하이픈(-)을 포함할 수 있습니다.")
		String phone,

		@NotBlank(message = "이메일은 필수 입력 항목입니다.")
		@Size(max = 50, message = "이메일은 50자 이내로 입력해주세요.")
		@Email(message = "유효한 이메일 형식이 아닙니다.")
		String email,

		@NotNull(message = "경력은 필수 입력 항목입니다.")
		@Positive(message = "경력은 0보다 큰 정수여야 합니다.")
		Integer career,

		@Size(max = 50, message = "현재 회사는 50자 이내로 입력해주세요.")
		String currentCompany,

		@NotBlank(message = "소개글은 필수 입력 항목입니다.")
		@Size(max = 500, message = "자기소개는 500자 이내로 입력해주세요.")
		String introduction,

		List<Long> attachmentId
	) {
	}
}
