package com.dementor.domain.admin.excption;

import lombok.Getter;

@Getter
public class AdminException extends RuntimeException {
	private final AdminErrorCode errorCode;

	public AdminException(AdminErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}


