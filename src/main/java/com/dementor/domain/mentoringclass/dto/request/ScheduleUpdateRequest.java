package com.dementor.domain.mentoringclass.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스케줄 수정 요청")
public record ScheduleUpdateRequest(
	@Schema(description = "스케줄 ID")
	Long scheduleId,

	@Schema(description = "요일")
	String dayOfWeek,

	@Schema(description = "시간")
	Integer time
) {
}