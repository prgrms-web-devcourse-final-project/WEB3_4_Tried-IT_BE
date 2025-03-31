package com.dementor.global.security.cookie;

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

	public ResponseCookie createJwtCookie(String token) {
		return ResponseCookie.from(cookieName, token)
			.httpOnly(httpOnly)
			.secure(secure)
			.path(path)
			.maxAge(maxAgeSeconds)
			.sameSite("Strict")
			.build();
	}

	public ResponseCookie deleteJwtCookie() {
		return ResponseCookie.from(cookieName, "")
			.httpOnly(httpOnly)
			.secure(secure)
			.path(path)
			.maxAge(0)
			.sameSite("Strict")
			.build();
	}
}
