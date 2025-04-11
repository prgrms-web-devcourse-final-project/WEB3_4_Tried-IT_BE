package com.dementor.domain.member.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import com.dementor.global.ApiResponse;
import com.dementor.global.security.CustomUserDetails;
import com.dementor.global.security.cookie.CookieUtil;
import com.dementor.global.security.jwt.JwtTokenProvider;
import com.dementor.global.security.jwt.dto.TokenDto;
import com.dementor.global.security.jwt.service.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "로그인, 로그아웃", description = "로그인, 로그아웃")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberAuthController {
	private final JwtTokenProvider jwtTokenProvider;
	private final AuthenticationManager authenticationManager;
	private final CookieUtil cookieUtil;
	private final TokenService tokenService;

	@Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<Void>> login(@RequestBody LoginRequest loginRequest) {
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
			TokenDto tokens = tokenService.createMemberTokens(authentication, memberId, nickname);

			// 쿠키에는 액세스 토큰만 저장,
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(tokens.getAccessToken()).toString());
			headers.add(HttpHeaders.SET_COOKIE,
				cookieUtil.createRefreshTokenCookie(tokens.getRefreshToken()).toString());

			return ResponseEntity.ok()
				.headers(headers)
				.body(ApiResponse.of(true, HttpStatus.OK, "로그인 성공"));

		} catch (AuthenticationException e) {
			return ResponseEntity.badRequest()
				.body(ApiResponse.of(false, HttpStatus.BAD_REQUEST, "로그인 실패"));
		}
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
		if (authentication != null) {
			String userIdentifier = authentication.getName();
			tokenService.logout(userIdentifier);
		}

		SecurityContextHolder.clearContext();

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteAccessTokenCookie().toString());
		headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteRefreshTokenCookie().toString());

		return ResponseEntity.ok()
			.headers(headers)
			.body(ApiResponse.of(true, HttpStatus.OK, "로그아웃 성공"));
	}

}
