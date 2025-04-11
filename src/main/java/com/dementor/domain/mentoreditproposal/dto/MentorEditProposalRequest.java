package com.dementor.domain.mentoreditproposal.dto;

import com.dementor.domain.mentor.entity.Mentor;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MentorEditProposalRequest {

	Long jobId;

	@Positive(message = "경력은 0보다 큰 정수여야 합니다.")
	Integer career;

	@Size(max = 50, message = "현재 회사는 50자 이내로 입력해주세요.")
	String currentCompany;

	@Size(max = 500, message = "자기소개는 500자 이내로 입력해주세요.")
	String introduction;

	List<Long> attachmentId;

	public boolean hasChanges(Mentor mentor) {
		return (career != null && !career.equals(mentor.getCareer())) ||
			(currentCompany != null && !currentCompany.equals(mentor.getCurrentCompany())) ||
			(jobId != null && !jobId.equals(mentor.getJob().getId())) ||
			(introduction != null && !introduction.equals(mentor.getIntroduction()));
	}

}
