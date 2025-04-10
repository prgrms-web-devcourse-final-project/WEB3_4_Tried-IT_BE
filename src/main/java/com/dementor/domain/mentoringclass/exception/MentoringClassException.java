package com.dementor.domain.mentoringclass.exception;

import lombok.Getter;

@Getter
public class MentoringClassException extends RuntimeException {
    private final MentoringClassExceptionCode errorCode;

    public MentoringClassException(MentoringClassExceptionCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
}
