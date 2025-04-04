package com.dementor.domain.mentor.dto.response;

import com.dementor.domain.mentor.entity.Mentor;

public record MentorInfoResponse (
        Long memberId,
        String name,
        String jobName,
        Integer career,
        String phone,
        String email,
        String currentCompany,
        String introduction,
        String bestFor,
        Mentor.ApprovalStatus approvalStatus,
        Integer totalClasses,
        Integer pendingRequests,
        Integer completedSessions
) {
    public static MentorInfoResponse from(Mentor mentor, Integer totalClasses,
                                          Integer pendingRequests, Integer completedSessions) {
        return new MentorInfoResponse(
                mentor.getId(),
                mentor.getName(),
                mentor.getJob().getName(),
                mentor.getCareer(),
                mentor.getPhone(),
                mentor.getEmail(),
                mentor.getCurrentCompany(),
                mentor.getIntroduction(),
                mentor.getBestFor(),
                mentor.getApprovalStatus(),
                totalClasses,
                pendingRequests,
                completedSessions
        );
    }
}
