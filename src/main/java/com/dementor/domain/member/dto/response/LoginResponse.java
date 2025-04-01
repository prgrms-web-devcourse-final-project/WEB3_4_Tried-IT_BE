package com.dementor.domain.member.dto.response;

import jakarta.validation.constraints.NotNull;
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
public class LoginResponse {
	// this is from 김호남남
	private Long id;

	@Schema(description = "닉네임")
	@NotNull
	private String nickname;

	@Schema(description = "메시지")
	@NotNull
	private String message;

	// this is from 김호남남
	@Schema(description = "JWT 토큰")
	private String token;
}
