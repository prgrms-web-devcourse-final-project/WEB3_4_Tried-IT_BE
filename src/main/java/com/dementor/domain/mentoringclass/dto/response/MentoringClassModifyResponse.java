package com.dementor.domain.mentoringclass.dto.response;

public record MentoringClassModifyResponse(
    Long classId,
    String stack,
    String content,
    String title,
    int price,
    Long schedule
) {
}
