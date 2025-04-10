package com.dementor.domain.postattachment.exception;

import lombok.Getter;

@Getter
public class PostAttachmentException extends RuntimeException {

    private final PostAttachmentErrorCode errorCode;

    public PostAttachmentException(PostAttachmentErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public PostAttachmentException(PostAttachmentErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}