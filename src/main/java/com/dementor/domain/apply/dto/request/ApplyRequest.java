package com.dementor.domain.apply.dto.request;

import java.time.LocalDateTime;

import com.dementor.domain.apply.entity.ApplyStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


public class ApplyRequest {

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ApplyCreateRequest {
		private Long classId;
		private String inquiry;
		private LocalDateTime schedule;
	}


	@Getter
	public static class ApplyStatusRequest {
		private ApplyStatus status;
	}

}