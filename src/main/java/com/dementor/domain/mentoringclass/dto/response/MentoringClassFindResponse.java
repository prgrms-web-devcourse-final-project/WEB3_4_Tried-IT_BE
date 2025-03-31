package com.dementor.domain.mentoringclass.dto.response;

public record MentoringClassFindResponse(
        Long classId,
        String stack,
        String content,
        String title,
        int price
) {
}


