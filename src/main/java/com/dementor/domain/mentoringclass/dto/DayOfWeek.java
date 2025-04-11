package com.dementor.domain.mentoringclass.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "요일")
public enum DayOfWeek {
	@Schema(description = "월요일")
	MONDAY,
	@Schema(description = "화요일")
	TUESDAY,
	@Schema(description = "수요일")
	WEDNESDAY,
	@Schema(description = "목요일")
	THURSDAY,
	@Schema(description = "금요일")
	FRIDAY,
	@Schema(description = "토요일")
	SATURDAY,
	@Schema(description = "일요일")
	SUNDAY
} 