package com.dementor.domain.mentoringclass.dto.response;

import com.dementor.domain.mentoringclass.entity.Schedule;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "수업 일정 응답")
public record ScheduleResponse(
        @Schema(description = "요일", example = "MONDAY")
        String dayOfWeek,
        @Schema(description = "시간", example = "14:00")
        int time
) {
    public static ScheduleResponse from(Schedule schedule) {
        return new ScheduleResponse(
                schedule.getDayOfWeek(),
                schedule.getTime()
        );
    }
} 