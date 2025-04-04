package com.dementor.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "로그인 응답")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberLoginResponse {
	@Schema(description = "메시지")
	private String message;

	@Schema(description = "JWT 토큰")
	private String accessToken;

	@Schema(description = "JWT refresh 토큰")
	private String refreshToken;
}
