package com.dementor.domain.mentor.dto.response;

import com.dementor.domain.apply.entity.Apply;
import com.dementor.domain.apply.entity.ApplyStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MentorApplyStatusResponse {
    private Long applymentId; // 신청 ID
    private Long classId;     // 클래스 ID
    private ApplyStatus status; // 변경된 상태
    
    public static MentorApplyStatusResponse from(Apply apply) {
        return MentorApplyStatusResponse.builder()
                .applymentId(apply.getId())
                .classId(apply.getMentoringClass().getId())
                .status(apply.getApplyStatus())
                .build();
    }
} 