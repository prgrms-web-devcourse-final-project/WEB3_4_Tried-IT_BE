package com.dementor.global.security.jwt;

public enum RefreshToken_Role {
	ROLE_MEMBER("ROLE_MEMBER"),
	ROLE_ADMIN("ROLE_ADMIN");

	private final String role;

	RefreshToken_Role(String role) {
		this.role = role;
	}
	public static RefreshToken_Role fromRole(String role) {
		if (role.equals("ROLE_ADMIN")) {return RefreshToken_Role.ROLE_ADMIN;}
		return RefreshToken_Role.ROLE_MEMBER;
	}
}
