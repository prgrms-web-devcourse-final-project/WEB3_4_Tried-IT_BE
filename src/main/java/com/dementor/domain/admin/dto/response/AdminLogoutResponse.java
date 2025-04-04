package com.dementor.domain.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AdminLogoutResponse {
	@Schema(description = "로그아웃 성공 여부")
	private boolean success;

	@Schema(description = "응답 메시지")
	private String message;
}
