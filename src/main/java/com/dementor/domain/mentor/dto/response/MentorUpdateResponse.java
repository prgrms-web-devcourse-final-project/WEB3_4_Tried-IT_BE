package com.dementor.domain.mentor.dto.response;

import com.dementor.domain.mentor.entity.Mentor;

public record MentorUpdateResponse(
        Long memberId,
        String modificationStatus
) {
    public static MentorUpdateResponse of(Long memberId, Mentor.ModificationStatus status) {
        return new MentorUpdateResponse(memberId, status.name());
    }
}
