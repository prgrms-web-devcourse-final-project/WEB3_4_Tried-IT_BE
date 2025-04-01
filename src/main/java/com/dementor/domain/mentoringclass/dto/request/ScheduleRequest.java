package com.dementor.domain.mentoringclass.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record ScheduleRequest(
    @Schema(description = "요일", example = "Monday")
    String dayOfWeek,
    @Schema(description = "가능 시간", example = "1400")
    int time
) {} 