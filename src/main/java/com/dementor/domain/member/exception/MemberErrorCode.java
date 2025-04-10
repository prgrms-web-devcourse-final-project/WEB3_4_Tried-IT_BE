package com.dementor.domain.member.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberErrorCode  {
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다"),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호 형식이 올바르지 않습니다"),
    INVALID_VERIFYCODE(HttpStatus.BAD_REQUEST, "인증번호가 유효하지 않습니다"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다"),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "올바르지 않은 인증번호입니다");

    
    private final HttpStatus status;
    private final String message;
}
