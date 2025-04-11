package com.dementor.domain.mentor.exception;

import lombok.Getter;

@Getter
public class MentorException extends RuntimeException {

	private final MentorErrorCode errorCode;

	public MentorException(MentorErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public MentorException(MentorErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
}
