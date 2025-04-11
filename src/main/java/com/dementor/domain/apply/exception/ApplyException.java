package com.dementor.domain.apply.exception;

import lombok.Getter;

@Getter
public class ApplyException extends RuntimeException {

	private final ApplyErrorCode errorCode;

	public ApplyException(ApplyErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

}
