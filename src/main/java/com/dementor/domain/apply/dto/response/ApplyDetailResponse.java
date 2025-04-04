package com.dementor.domain.apply.dto.response;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.dementor.domain.apply.entity.Apply;
import com.dementor.domain.apply.entity.ApplyStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplyDetailResponse {
	private Long applymentId;
	private Long classId;
	private Long mentorId; //멘토 아이디
	private String name; //멘토 이름
	private ApplyStatus status;
	private String inquiry;
	private ZonedDateTime schedule;

	public static ApplyDetailResponse from(Apply apply) {
		return ApplyDetailResponse.builder()
			.applymentId(apply.getId())
			.classId(apply.getMentoringClass().getId())
			.mentorId(apply.getMentoringClass().getMentor().getId())
			.name(apply.getMentoringClass().getMentor().getName())
			.status(apply.getApplyStatus())
			.inquiry(apply.getInquiry())
			.schedule(apply.getSchedule().atZone(ZoneId.systemDefault()))
			.build();
	}
}
