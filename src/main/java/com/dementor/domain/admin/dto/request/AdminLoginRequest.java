package com.dementor.domain.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "관리자 로그인 요청")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdminLoginRequest {

	@NotNull
	private String username;

	@NotNull
	private String password;
}
