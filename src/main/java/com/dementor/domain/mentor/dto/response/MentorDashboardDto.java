package com.dementor.domain.mentor.dto.response;

import java.util.List;

public class MentorDashboardDto {

    //멘토 대시보드 조회 응답 DTO
    public record DashboardResponseDto(
            MentorInfo mentorInfo,
            Integer pendingRequests,
            List<UpcomingSession> upcomingSession,
            Integer completedSessions
    ) {
        //멘토 정보
        public record MentorInfo(
                Long mentorId,
                String name,
                String job,
                Integer career,
                String phone,
                String stack,
                Boolean isApproved,
                Integer totalClasses
        ) {}

        //예정된 멘토링 세션
        public record UpcomingSession(
                Long applymentId,
                String menteeName,
                String className,
                String date,
                String timeSlot
        ) {}
    }
}
