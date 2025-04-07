package com.dementor.domain.mentor.dto.request;

import com.dementor.domain.apply.entity.ApplyStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MentorApplyStatusRequest {
    @Schema(description = "신청 상태", example = "APPROVED or REJECTED")
    private ApplyStatus status; // "APPROVED" 또는 "REJECTED"
} 