package com.dementor.global.security.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {
	@Value("${jwt.cookie.name}")
	private String cookieName;

	@Value("${jwt.cookie.max-age-seconds}")
	private long maxAgeSeconds;

	@Value("${jwt.cookie.http-only}")
	private boolean httpOnly;

	@Value("${jwt.cookie.secure}")
	private boolean secure;

	@Value("${jwt.cookie.path}")
	private String path;

	//액세스 토큰을 담은 쿠키 생성
	public ResponseCookie createJwtCookie(String token) {
		return ResponseCookie.from(cookieName, token)
			.httpOnly(httpOnly)
			.secure(secure)
			.path(path)
			.maxAge(maxAgeSeconds)
			.sameSite("Strict")
			.build();
	}

	//쿠키 삭제용
	public ResponseCookie deleteJwtCookie() {
		return ResponseCookie.from(cookieName, "")
			.httpOnly(httpOnly)
			.secure(secure)
			.path(path)
			.maxAge(0)
			.sameSite("Strict")
			.build();
	}

	// [추가] 요청에서 쿠키로부터 access_token 꺼내는 메서드
	public String getTokenFromCookie(HttpServletRequest request) {
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (cookieName.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		throw new IllegalArgumentException("JWT 토큰이 담긴 쿠키가 존재하지 않습니다.");
	}
}
