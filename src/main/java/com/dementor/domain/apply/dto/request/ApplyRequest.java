package com.dementor.domain.apply.dto.request;

import java.time.LocalDateTime;

import com.dementor.domain.apply.entity.ApplyStatus;

import lombok.Getter;

public class ApplyRequest {

	@Getter
	public static class ApplyCreateRequest {
		private Long class_id;
		private String inquiry;
		private String year_month;
		private LocalDateTime schedule;//신청 날짜
	}

	@Getter
	public static class ApplyUpdateRequest {
		private Long applyment_id;
		private String year_month;
		private LocalDateTime schedule;//신청 날짜
	}

	@Getter
	public static class ApplyApproveRequest {
		private ApplyStatus status;
	}

}
