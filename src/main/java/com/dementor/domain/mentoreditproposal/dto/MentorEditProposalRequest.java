package com.dementor.domain.mentoreditproposal.dto;

import java.util.List;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class MentorEditProposalRequest {

	Long jobId;

	@Positive(message = "경력은 0보다 큰 정수여야 합니다.")
	Integer career;

	@Size(max = 50, message = "현재 회사는 50자 이내로 입력해주세요.")
	String currentCompany;

	@Size(max = 500, message = "자기소개는 500자 이내로 입력해주세요.")
	String introduction;

	List<Long> attachmentId;

}
