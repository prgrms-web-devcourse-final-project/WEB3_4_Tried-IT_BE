package com.dementor.domain.mentoringclass.dto.request;

import java.util.List;

public record MentoringClassCreateRequest(
        String stack,
        String content,
        String title,
        int price,
        List<ScheduleRequest> schedules
) {
}
