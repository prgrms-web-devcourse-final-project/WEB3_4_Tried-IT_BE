package com.dementor.global.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.dementor.domain.member.exception.MemberErrorCode;
import com.dementor.domain.member.exception.MemberException;
import com.dementor.global.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MemberException.class)
	public ApiResponse<?> handleMemberException(MemberException e) {
		MemberErrorCode errorCode = e.getErrorCode();
		return ApiResponse.of(false,
			errorCode.getStatus(),
			errorCode.getMessage());
	}
}