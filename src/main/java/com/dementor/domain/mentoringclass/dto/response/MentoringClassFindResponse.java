package com.dementor.domain.mentoringclass.dto.response;

import com.dementor.domain.mentoringclass.entity.MentoringClass;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "멘토링 수업 조회 응답")
public record MentoringClassFindResponse(
        @Schema(description = "수업 ID", example = "1")
        Long classId,
        @Schema(description = "멘토 정보")
        MentorInfo mentor,
        @Schema(description = "기술 스택 목록", example = "[\"Java\", \"Spring Boot\", \"MySQL\"]")
        String[] stack,
        @Schema(description = "수업 내용", example = "스프링 부트 기초부터 실전까지")
        String content,
        @Schema(description = "수업 제목", example = "스프링 부트 완전 정복")
        String title,
        @Schema(description = "수업 가격", example = "50000")
        int price
) {
    public record MentorInfo(
            @Schema(description = "멘토 ID")
            Long mentorId,
            @Schema(description = "멘토 이름")
            String name,
            @Schema(description = "멘토 직무")
            String job,
            @Schema(description = "멘토 경력")
            int career
    ) {
    }

    public static MentoringClassFindResponse from(MentoringClass mentoringClass) {
        return new MentoringClassFindResponse(
                mentoringClass.getId(),
                new MentoringClassFindResponse.MentorInfo(
                        mentoringClass.getMentor().getId(),
                        mentoringClass.getMentor().getName(),
                        mentoringClass.getMentor().getJob().getName(),
                        mentoringClass.getMentor().getCareer()
                ),
                mentoringClass.getStack().split(","),
                mentoringClass.getContent(),
                mentoringClass.getTitle(),
                mentoringClass.getPrice()
        );
    }
}


