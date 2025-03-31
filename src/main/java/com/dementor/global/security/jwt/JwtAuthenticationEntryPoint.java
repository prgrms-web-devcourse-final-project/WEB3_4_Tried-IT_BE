package com.dementor.global.security.jwt;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


//유효한 자격증명을 제공하지 않고 접근 -> 401 Unauthorized 에러 리턴 클래스
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException authException) throws IOException {
		// 인증되지 않은 요청에 대한 처리
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증에 실패했습니다");
	}
}
