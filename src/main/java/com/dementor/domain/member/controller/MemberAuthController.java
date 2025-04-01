package com.dementor.domain.member.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dementor.domain.member.dto.request.LoginRequest;
import com.dementor.domain.member.dto.response.LoginResponse;
import com.dementor.global.security.CustomUserDetails;
import com.dementor.global.security.cookie.CookieUtil;
import com.dementor.global.security.jwt.JwtTokenProvider;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;

@Tag(name = "로그인, 로그아웃", description = "로그인, 로그아웃")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberAuthController {
	private final JwtTokenProvider jwtTokenProvider;
	private final AuthenticationManager authenticationManager;
	private final CookieUtil cookieUtil;

	@Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
		try {
			// 인증 시도
			Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
					loginRequest.getEmail(),
					loginRequest.getPassword()
				)
			);

			// Security Context에 인증 정보 저장
			SecurityContextHolder.getContext().setAuthentication(authentication);

			// 사용자 정보 가져오기
			CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();
			Long memberId = userDetails.getId();
			String nickname = userDetails.getNickname();

			// JWT 토큰 생성 (memberId와 nickname 포함)
			String jwt = jwtTokenProvider.createToken(authentication, memberId, nickname);

			// 쿠키 생성 (JWT 토큰만 포함하는 방식으로 변경)
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.SET_COOKIE, cookieUtil.createJwtCookie(jwt).toString());


			// this is from 김호남남
			return ResponseEntity.ok()
				.headers(headers)
				.body(LoginResponse.builder()
					.nickname(nickname)
					.message("로그인 성공")
					.token(jwt)
					.build());

		} catch (AuthenticationException e) {
			return ResponseEntity.badRequest()
				.body(LoginResponse.builder()
					.nickname(null)
					.message("로그인 실패: " + e.getMessage())
					.token(null)
					.build());
		}
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout() {

		SecurityContextHolder.clearContext();

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteJwtCookie().toString());

		return ResponseEntity.ok()
			.headers(headers)
			.body(new LoginResponse(null, null, "로그아웃 성공", null));
	}
}
