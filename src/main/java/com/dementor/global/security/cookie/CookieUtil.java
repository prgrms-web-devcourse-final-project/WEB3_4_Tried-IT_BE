package com.dementor.global.security.cookie;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

	@Value("${jwt.cookie.access-cookie.name}")
	private String accessCookieName;

	@Value("${jwt.cookie.refresh-cookie.name}")
	private String refreshCookieName;

	@Value("${jwt.cookie.max-age-seconds}")
	private long maxAgeSeconds;

	@Value("${jwt.cookie.http-only}")
	private boolean httpOnly;

	@Value("${jwt.cookie.secure}")
	private boolean secure;

	@Value("${jwt.cookie.path}")
	private String path;

	@Value("${jwt.cookie.domain}")
	private String domain;

	public ResponseCookie createAccessTokenCookie(String token) {
		return ResponseCookie.from(accessCookieName, token)
			.httpOnly(httpOnly)
			.secure(secure)
			.path(path)
			.domain(domain)
			.maxAge(maxAgeSeconds)
			.sameSite("Strict")
			.build();
	}

	public ResponseCookie deleteAccessTokenCookie() {
		return ResponseCookie.from(accessCookieName, "")
			.httpOnly(httpOnly)
			.secure(secure)
			.path(path)
			.domain(domain)
			.maxAge(0)
			.sameSite("Strict")
			.build();
	}

	public ResponseCookie createRefreshTokenCookie(String token) {
		return ResponseCookie.from(refreshCookieName, token)
			.httpOnly(httpOnly)
			.secure(secure)
			.path(path)
			.domain(domain)
			.maxAge(maxAgeSeconds)
			.sameSite("Strict")
			.build();
	}

	public ResponseCookie deleteRefreshTokenCookie() {
		return ResponseCookie.from(refreshCookieName, "")
			.httpOnly(httpOnly)
			.secure(secure)
			.path(path)
			.domain(domain)
			.maxAge(0)
			.sameSite("Strict")
			.build();
	}
}
