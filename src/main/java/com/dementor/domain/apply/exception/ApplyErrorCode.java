package com.dementor.domain.apply.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApplyErrorCode {

	SCHEDULE_REQUIRED(HttpStatus.BAD_REQUEST, "멘토링 일정을 선택해야 합니다."),
	APPLY_NOT_FOUND(HttpStatus.NOT_FOUND, "멘토링을 찾을 수 없습니다."),
	NOT_YOUR_APPLY(HttpStatus.FORBIDDEN, "본인이 신청한 멘토링만 취소할 수 있습니다."),
	CAN_NOT_APPLY_YOUR_CLASS(HttpStatus.FORBIDDEN, "자신의 멘토링 클래스에 신청할 수 없습니다.");

	private final HttpStatus status;
	private final String message;
}
