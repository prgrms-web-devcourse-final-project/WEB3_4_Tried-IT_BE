package com.dementor.domain.member.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.dementor.global.ApiResponse;

@RestControllerAdvice(basePackages = "com.dementor.domain.member")  // 멘토링 클래스 도메인에서만 동작
@Order(Ordered.HIGHEST_PRECEDENCE)  // GlobalExceptionHandler보다 먼저 처리되도록 설정
public class MemberExceptionHandler {
	@ExceptionHandler(MemberException.class)
	public ResponseEntity<ApiResponse<Void>> handleMemberException(MemberException e) {
		MemberErrorCode errorCode = e.getErrorCode();
		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ApiResponse.of(
				false,
				errorCode.getStatus(),
				errorCode.getMessage()
			));
	}
}
