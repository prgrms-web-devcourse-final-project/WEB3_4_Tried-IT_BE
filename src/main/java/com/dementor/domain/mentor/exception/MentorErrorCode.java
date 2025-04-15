package com.dementor.domain.mentor.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MentorErrorCode {

	MENTOR_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 멘토를 찾을 수 없습니다."),
	NOT_APPROVED_MENTOR(HttpStatus.FORBIDDEN, "승인되지 않은 멘토는 이 기능을 사용할 수 없습니다."),
	MENTOR_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 멘토로 등록되어 있습니다."),
	INVALID_MENTOR_APPLICATION(HttpStatus.BAD_REQUEST, "유효하지 않은 멘토 지원 정보입니다."),
	INVALID_PAGE_PARAMS(HttpStatus.BAD_REQUEST, "유효하지 않은 페이지 파라미터입니다."),
	INVALID_STATUS_PARAM(HttpStatus.BAD_REQUEST, "유효하지 않은 상태 파라미터입니다."),
	UNAUTHORIZED_MODIFICATION(HttpStatus.FORBIDDEN, "해당 멘토 정보를 수정할 권한이 없습니다."),
	UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "해당 멘토 정보에 접근할 권한이 없습니다."),
	JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "직무 정보를 찾을 수 없습니다."),
	EDIT_PROPOSAL_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 수정 요청을 찾을 수 없습니다.");

	private final HttpStatus status;
	private final String message;
}
