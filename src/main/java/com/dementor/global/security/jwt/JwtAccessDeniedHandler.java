package com.dementor.global.security.jwt;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//필요한 권한이 존재하지 않는 경우 -> 403 Forbidden 에러 리턴
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(
		HttpServletRequest request,
		HttpServletResponse response,
		AccessDeniedException accessDeniedException) throws IOException {
		String origin = request.getHeader("Origin");
		if (origin != null) {
			response.setHeader("Access-Control-Allow-Origin", origin);
			response.setHeader("Access-Control-Allow-Credentials", "true");
		}

		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.getWriter().write("{\"message\": \"접근 권한이 없습니다\"}");
	}
}
