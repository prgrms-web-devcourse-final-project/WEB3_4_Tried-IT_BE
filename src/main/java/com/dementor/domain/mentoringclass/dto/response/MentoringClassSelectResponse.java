package com.dementor.domain.mentoringclass.dto.response;

public record MentoringClassSelectResponse(
        Long classId,
        MentorInfoResponse mentor,
        String stack,
        String content,
        String title,
        int price
) {
}
