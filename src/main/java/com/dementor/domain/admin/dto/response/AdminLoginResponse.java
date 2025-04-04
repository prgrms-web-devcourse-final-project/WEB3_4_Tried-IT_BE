package com.dementor.domain.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "관리자 로그인 응답")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminLoginResponse {
	@Schema(description = "메시지")
	private String message;

	@Schema(description = "JWT 토큰")
	private String accessToken;

	@Schema(description = "JWT refresh 토큰")
	private String refreshToken;
} 