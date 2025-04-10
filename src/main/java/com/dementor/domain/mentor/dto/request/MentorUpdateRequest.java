package com.dementor.domain.mentor.dto.request;

import java.util.List;

import com.dementor.domain.mentor.entity.Mentor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class MentorUpdateRequest {
    public record MentorUpdateRequestDto(
            @Positive(message = "경력은 0보다 큰 정수여야 합니다.")
            Integer career,

            @Size(max = 20, message = "전화번호는 20자 이내로 입력해주세요.")
            @Pattern(regexp = "^[\\d-]{10,13}$", message = "전화번호는 10~11자리 숫자만 입력 가능합니다.")
            String phone,

            @Size(max = 50, message = "현재 회사는 50자 이내로 입력해주세요.")
            String currentCompany,

            Long jobId,

            @Size(max = 50, message = "이메일은 50자 이내로 입력해주세요.")
            @Email(message = "유효한 이메일 형식이 아닙니다.")
            String email,

            @Size(max = 500, message = "자기소개는 500자 이내로 입력해주세요.")
            String introduction,

            @Size(max = 500, message = "추천대상은 500자 이내로 입력해주세요.")
            String bestFor,

            List<Long> attachmentId
    ) {
        //변경된 필드가 있는지 확인
        public boolean hasChanges(Mentor mentor) {
            return (career != null && !career.equals(mentor.getCareer())) ||
                    (phone != null && !phone.equals(mentor.getPhone())) ||
                    (currentCompany != null && !currentCompany.equals(mentor.getCurrentCompany())) ||
                    (jobId != null && !jobId.equals(mentor.getJob().getId())) ||
                    (email != null && !email.equals(mentor.getMember().getEmail())) ||
                    (introduction != null && !introduction.equals(mentor.getIntroduction()));
        }
    }
}
