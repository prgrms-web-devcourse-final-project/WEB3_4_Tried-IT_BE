package com.dementor.domain.mentoringclass.dto.response;

public record MentorInfoResponse(
    Long mentorId,
    String name,
    String job,
    int career
) {
} 