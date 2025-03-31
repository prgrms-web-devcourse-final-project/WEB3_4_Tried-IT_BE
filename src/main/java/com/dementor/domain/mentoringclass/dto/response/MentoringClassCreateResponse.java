package com.dementor.domain.mentoringclass.dto.response;

public record MentoringClassCreateResponse(
        Long classId,
        Long mentorId,
        String stack,
        String content,
        String title,
        int price
) {
}
