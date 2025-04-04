package com.dementor.domain.mentor.dto.response;

public record MyMentoringResponse(
    Long classId,
    String[] stack,
    String content,
    String title,
    int price
) {
}
