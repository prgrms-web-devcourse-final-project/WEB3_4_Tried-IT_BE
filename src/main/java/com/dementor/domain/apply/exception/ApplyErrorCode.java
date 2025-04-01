package com.dementor.domain.apply.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApplyErrorCode {

	SCHEDULE_REQUIRED(HttpStatus.BAD_REQUEST, "멘토링 일정을 선택해야 합니다.");


	private final HttpStatus status;
	private final String message;
}
