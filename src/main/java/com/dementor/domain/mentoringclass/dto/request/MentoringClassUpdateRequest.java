package com.dementor.domain.mentoringclass.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "멘토링 수업 수정 요청")
public record MentoringClassUpdateRequest(
	@Schema(description = "수업 제목")
	String title,

	@Schema(description = "수업 내용")
	String content,

	@Schema(description = "수업 가격")
	Integer price,

	@Schema(description = "기술 스택 목록", example = "[\"Java\", \"Spring Boot\", \"MySQL\"]")
	String[] stack,

	@Schema(description = "스케줄 정보")
	ScheduleRequest schedule
) {
}