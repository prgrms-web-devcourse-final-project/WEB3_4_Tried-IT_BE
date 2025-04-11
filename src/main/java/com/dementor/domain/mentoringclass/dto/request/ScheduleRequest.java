package com.dementor.domain.mentoringclass.dto.request;

import com.dementor.domain.mentoringclass.dto.DayOfWeek;

import io.swagger.v3.oas.annotations.media.Schema;

public record ScheduleRequest(
	@Schema(description = "요일", example = "MONDAY")
	DayOfWeek dayOfWeek,
	@Schema(description = "가능 시간", example = "14:00")
	String time
) {
}