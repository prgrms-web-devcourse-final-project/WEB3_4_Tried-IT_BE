package com.dementor.domain.mentoringclass.dto.response;

import com.dementor.domain.mentoringclass.dto.DayOfWeek;
import io.swagger.v3.oas.annotations.media.Schema;

public record MentoringClassUpdateResponse(
    @Schema(description = "수업 ID")
    Long classId,
    
    @Schema(description = "멘토 정보")
    MentorInfo mentor,
    
    @Schema(description = "기술 스택 목록", example = "[\"Java\", \"Spring Boot\", \"MySQL\"]")
    String[] stack,
    
    @Schema(description = "수업 내용")
    String content,
    
    @Schema(description = "수업 제목")
    String title,
    
    @Schema(description = "수업 가격")
    int price,
    
    @Schema(description = "스케줄 정보")
    ScheduleInfo schedule
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
    ) {}
    
    public record ScheduleInfo(
        @Schema(description = "요일")
        DayOfWeek dayOfWeek,
        
        @Schema(description = "시간")
        String time
    ) {}
}
