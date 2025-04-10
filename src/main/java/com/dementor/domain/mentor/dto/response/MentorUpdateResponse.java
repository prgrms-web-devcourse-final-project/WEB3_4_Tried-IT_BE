package com.dementor.domain.mentor.dto.response;

import com.dementor.domain.mentor.entity.ModificationStatus;

public record MentorUpdateResponse(
        Long memberId,
        String modificationStatus
) {
    public static MentorUpdateResponse of(Long memberId, ModificationStatus status) {
        return new MentorUpdateResponse(memberId, status.name());
    }
}
