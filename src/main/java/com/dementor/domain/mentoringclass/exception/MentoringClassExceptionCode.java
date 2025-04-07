package com.dementor.domain.mentoringclass.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MentoringClassExceptionCode {
    MENTORING_CLASS_NOT_FOUND(HttpStatus.NOT_FOUND, "멘토링을 찾을 수 없습니다."),
    MENTORING_CLASS_UNAUTHORIZED(HttpStatus.FORBIDDEN, "해당 멘토링 클래스를 수정할 권한이 없습니다."),
    TITLE_OR_CONTENT_INPUT_NULL(HttpStatus.BAD_REQUEST, "제목 또는 내용은 필수입니다."),
    MINUS_PRICE(HttpStatus.BAD_REQUEST, "가격이 음수입니다."),
    EMPTY_SCHEDULE(HttpStatus.BAD_REQUEST, "일정 선택은 필수입니다."),
    EMPTY_STACK(HttpStatus.BAD_REQUEST, "기술 스택 정보는 필수입니다.")
    ;

    private final HttpStatus status;
    private final String message;
}
