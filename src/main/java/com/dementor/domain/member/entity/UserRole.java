package com.dementor.domain.member.entity;

public enum UserRole {
	MENTOR("ROLE_MENTOR"), //멘토 이자 멘티
	MENTEE("ROLE_MENTEE"); // 멘티

	private final String role;
	UserRole(String role) {
		this.role = role;
	}

	public static UserRole fromRole(String role) {
		if (role.equals("ROLE_MENTOR")) {return UserRole.MENTOR;}
		return UserRole.MENTEE;
	}
}
