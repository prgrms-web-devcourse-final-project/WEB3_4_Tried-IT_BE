package com.dementor.domain.postattachment.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostAttachmentErrorCode {

	FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."),
	FILE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 파일에 접근할 권한이 없습니다."),
	FILE_DELETE_NO_PERMISSION(HttpStatus.FORBIDDEN, "해당 파일을 삭제할 권한이 없습니다."),
	FILE_UPLOAD_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 업로드 제한을 초과했습니다."),
	FILE_REQUIRED(HttpStatus.BAD_REQUEST, "파일이 첨부되지 않았습니다."),
	FILE_SIZE_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "파일 크기가 허용 범위를 초과했습니다."),
	INVALID_FILE_PATH(HttpStatus.BAD_REQUEST, "잘못된 파일 경로입니다."),
	NOT_MARKDOWN_IMAGE(HttpStatus.BAD_REQUEST, "요청한 파일은 마크다운 이미지가 아닙니다."),
	FILE_READ_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일을 읽을 수 없습니다."),
	UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
	FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 중 오류가 발생했습니다."),
	APPLY_PROPOSAL_NOT_FOUND(HttpStatus.NOT_FOUND, "멘토 지원서를 찾을 수 없습니다."),
	EDIT_PROPOSAL_NOT_FOUND(HttpStatus.NOT_FOUND, "멘토 정보 수정 요청을 찾을 수 없습니다.");

	private final HttpStatus status;
	private final String message;
}
