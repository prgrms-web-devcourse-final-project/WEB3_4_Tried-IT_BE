package com.dementor.domain.mentoringclass.dto.response;

import com.dementor.domain.mentoringclass.entity.MentoringClass;
import com.dementor.domain.mentoringclass.entity.Schedule;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "멘토링 수업 상세 조회 응답")
public record MentoringClassDetailResponse(
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
	int price,
	@Schema(description = "수업 일정 목록")
	List<ScheduleResponse> schedules
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

	public static MentoringClassDetailResponse from(MentoringClass mentoringClass, List<Schedule> schedulesInfo) {
		return new MentoringClassDetailResponse(
			mentoringClass.getId(),
			new MentorInfo(
				mentoringClass.getMentor().getId(),
				mentoringClass.getMentor().getName(),
				mentoringClass.getMentor().getJob().getName(),
				mentoringClass.getMentor().getCareer()
			),
			mentoringClass.getStack(),
			mentoringClass.getContent(),
			mentoringClass.getTitle(),
			mentoringClass.getPrice(),
			schedulesInfo.stream()
				.map(ScheduleResponse::from)
				.toList()
		);
	}
} 