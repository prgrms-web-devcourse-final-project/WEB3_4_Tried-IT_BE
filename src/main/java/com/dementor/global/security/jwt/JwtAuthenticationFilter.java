package com.dementor.global.security.jwt;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dementor.global.security.cookie.CookieUtil;
import com.dementor.global.security.jwt.dto.TokenDto;
import com.dementor.global.security.jwt.service.TokenService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;
	private final TokenService tokenService;
	private final CookieUtil cookieUtil;

	//doFilter의 역할은 토큰의 실제 인증정보를 현재 실행중인 securityContext 에 저장하는 역할
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		// 리프레시 엔드포인트는 리프레시 토큰 검증
		if (request.getRequestURI().equals("/api/admin/refresh") || request.getRequestURI()
			.equals("/api/member/refresh")) {
			String refreshToken = resolveRefreshToken(request);
			if (StringUtils.hasText(refreshToken) && jwtTokenProvider.validateRefreshToken(refreshToken)) {
				Authentication auth = jwtTokenProvider.getRefreshAuthentication(refreshToken);
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
			filterChain.doFilter(request, response);
			return;
		}

		String accessToken = resolveAccessToken(request);

		// 일반 엔드포인트는 액세스 토큰 검증
		if (StringUtils.hasText(accessToken)) {
			if (jwtTokenProvider.validateAccessToken(accessToken)) {
				Authentication auth = jwtTokenProvider.getAuthentication(accessToken);
				System.out.println("Authorities: " + auth.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(auth);
			} else {
				// 액세스 토큰이 만료된 경우 리프레시 토큰으로 갱신 시도
				String refreshToken = resolveRefreshToken(request);
				if (StringUtils.hasText(refreshToken)) {
					try {
						if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
							// 리프레시 토큰이 만료된 경우 로그아웃 처리
							response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.deleteAccessTokenCookie().toString());
							response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.deleteRefreshTokenCookie().toString());
							response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
							response.getWriter().write("{\"error\":\"REFRESH_TOKEN_EXPIRED\",\"message\":\"리프레시 토큰이 만료되었습니다. 다시 로그인해주세요.\"}");
							return;
						}
						
						TokenDto newTokens = tokenService.refreshAccessToken(refreshToken);
						Authentication auth = jwtTokenProvider.getAuthentication(newTokens.getAccessToken());
						SecurityContextHolder.getContext().setAuthentication(auth);

						response.addHeader(
							HttpHeaders.SET_COOKIE,
							cookieUtil.createAccessTokenCookie(newTokens.getAccessToken()).toString());
						response.addHeader(HttpHeaders.SET_COOKIE,
							cookieUtil.createRefreshTokenCookie(newTokens.getRefreshToken()).toString());
					} catch (Exception e) {
						// 토큰 재발급 실패 시 로그아웃 처리
						response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.deleteAccessTokenCookie().toString());
						response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.deleteRefreshTokenCookie().toString());
						response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
						response.getWriter().write("{\"error\":\"TOKEN_REFRESH_FAILED\",\"message\":\"토큰 갱신에 실패했습니다. 다시 로그인해주세요.\"}");
						return;
					}
				} else {
					// 리프레시 토큰이 없는 경우 로그아웃 처리
					response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.deleteAccessTokenCookie().toString());
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					response.getWriter().write("{\"error\":\"NO_REFRESH_TOKEN\",\"message\":\"리프레시 토큰이 없습니다. 다시 로그인해주세요.\"}");
					return;
				}
			}
		}

		filterChain.doFilter(request, response);
	}

	private String resolveAccessToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7).trim();
		}

		if (request.getCookies() == null) {
			return null;
		}

		Optional<Cookie> tokenCookie = Arrays.stream(request.getCookies())
			.filter(cookie -> cookieUtil.getAccessCookieName().equals(cookie.getName()))
			.findFirst();

		return tokenCookie.map(Cookie::getValue).orElse(null);
	}

	private String resolveRefreshToken(HttpServletRequest request) {
		if (request.getCookies() == null) {
			return null;
		}

		Optional<Cookie> tokenCookie = Arrays.stream(request.getCookies())
			.filter(cookie -> cookieUtil.getRefreshCookieName().equals(cookie.getName()))
			.findFirst();

		return tokenCookie.map(Cookie::getValue).orElse(null);
	}

}
