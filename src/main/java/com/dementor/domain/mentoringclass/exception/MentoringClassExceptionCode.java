package com.dementor.domain.mentoringclass.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MentoringClassExceptionCode {
    MENTORING_CLASS_NOT_FOUND(HttpStatus.NOT_FOUND, "멘토링을 찾을 수 없습니다."),
    MENTORING_CLASS_NOT_YOUR(HttpStatus.FORBIDDEN, "본인이 만드는 멘토링만 참고할 수 있습니다."),
    MENTORING_CLASS_UNAUTHORIZED(HttpStatus.FORBIDDEN, "해당 멘토링 클래스를 수정할 권한이 없습니다.");

    private final HttpStatus status;
    private final String message;
}
