package com.dementor.domain.mentoringclass.dto.request;

public record MentoringClassCreateRequest(
        Long mentorId, //TODO : mentor dto 참조해서 수정
        String stack,
        String content,
        String title,
        int price
) {
}
