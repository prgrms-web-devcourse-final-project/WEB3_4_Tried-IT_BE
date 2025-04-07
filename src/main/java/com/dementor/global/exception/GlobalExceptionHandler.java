package com.dementor.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.dementor.domain.apply.exception.ApplyErrorCode;
import com.dementor.domain.apply.exception.ApplyException;
import com.dementor.global.ApiResponse;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApplyException.class)
	public ApiResponse<?> handleApplyException(ApplyException e) {
		ApplyErrorCode errorCode = e.getErrorCode();
		return ApiResponse.of(false,
			errorCode.getStatus(),
			errorCode.getMessage());
	}

	// EntityNotFoundException 처리
	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<ApiResponse<?>> handleEntityNotFoundException(EntityNotFoundException e) {
		return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(ApiResponse.of(false, HttpStatus.NOT_FOUND, e.getMessage()));
	}

	// IllegalStateException 처리
	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ApiResponse<?>> handleIllegalStateException(IllegalStateException e) {
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.of(false, HttpStatus.BAD_REQUEST, e.getMessage()));
	}

	// 일반 예외 처리
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception e) {
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.of(false, HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다: " + e.getMessage()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ApiResponse<?> handleIllegalArgumentException(IllegalArgumentException e) {
		return ApiResponse.of(false,
			HttpStatus.BAD_REQUEST,
			e.getMessage());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ApiResponse<?> handleAccessDeniedException(AccessDeniedException e) {
		return ApiResponse.of(false,
			HttpStatus.FORBIDDEN,
			e.getMessage());
	}
}