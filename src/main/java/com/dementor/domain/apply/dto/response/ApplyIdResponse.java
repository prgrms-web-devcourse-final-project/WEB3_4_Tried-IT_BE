package com.dementor.domain.apply.dto.response;

import com.dementor.domain.apply.entity.Apply;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplyIdResponse {
	private Long applyId;

	public static ApplyIdResponse from(Apply apply) {
		return ApplyIdResponse.builder()
			.applyId(apply.getId())
			.build();
	}
}
