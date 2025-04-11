package com.dementor.domain.apply.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.dementor.domain.apply.entity.Apply;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplyScheduleResponse {
	private List<ScheduleItem> applyments;

	public static ApplyScheduleResponse fromList(List<Apply> applies) {
		List<ScheduleItem> scheduleItems = applies.stream()
			.map(apply -> ScheduleItem.builder()
				.schedule(apply.getSchedule())
				.build())
			.collect(Collectors.toList());

		return ApplyScheduleResponse.builder()
			.applyments(scheduleItems)
			.build();
	}

	@Getter
	@Builder
	public static class ScheduleItem {
		private LocalDateTime schedule;
	}
} 