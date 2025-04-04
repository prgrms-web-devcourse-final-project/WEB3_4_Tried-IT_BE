package com.dementor.domain.mentoringclass.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "멘토링 수업 생성 요청")
public record MentoringClassCreateRequest(
        @Schema(description = "기술 스택 목록", example = "[\"Java\", \"Spring Boot\", \"MySQL\"]")
        String[] stack,
        @Schema(description = "수업 내용", example = "스프링 부트 기초부터 실전까지")
        String content,
        @Schema(description = "수업 제목", example = "스프링 부트 완전 정복")
        String title,
        @Schema(description = "수업 가격", example = "50000")
        int price,
        @Schema(description = "수업 일정 목록")
        List<ScheduleRequest> schedules
) {
}
