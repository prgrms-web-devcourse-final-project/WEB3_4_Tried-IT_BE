package com.dementor.domain.mentoringclass.dto.response;

import com.dementor.domain.mentoringclass.entity.MentoringClass;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "멘토링 수업 상세 조회 응답")
public record MentoringClassDetailResponse(
        @Schema(description = "수업 ID", example = "1")
        Long classId,
        @Schema(description = "기술 스택", example = "Java, Spring Boot")
        String stack,
        @Schema(description = "수업 내용", example = "스프링 부트 기초부터 실전까지")
        String content,
        @Schema(description = "수업 제목", example = "스프링 부트 완전 정복")
        String title,
        @Schema(description = "수업 가격", example = "50000")
        int price,
        @Schema(description = "직무 이름", example = "백엔드 개발자")
        String jobName,
        @Schema(description = "멘토 이름", example = "홍길동")
        String mentorName,
        @Schema(description = "수업 일정 목록")
        List<ScheduleResponse> schedules
) {
    public static MentoringClassDetailResponse from(MentoringClass mentoringClass) {
        return new MentoringClassDetailResponse(
                mentoringClass.getId(),
                mentoringClass.getStack(),
                mentoringClass.getContent(),
                mentoringClass.getTitle(),
                mentoringClass.getPrice(),
                mentoringClass.getMentor().getJob().getName(),
                mentoringClass.getMentor().getName(),
                mentoringClass.getSchedules().stream()
                        .map(ScheduleResponse::from)
                        .toList()
        );
    }
} 