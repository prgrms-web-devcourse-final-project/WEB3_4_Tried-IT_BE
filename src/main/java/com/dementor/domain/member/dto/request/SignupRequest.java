package com.dementor.domain.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupRequest {
	@NotNull
	@Email
	private String email;

	@NotNull
	private String password;

	@NotNull
	private String nickname;

	@NotNull
	private String name;

	@NotNull
	private String verifyCode;

}
