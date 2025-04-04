package com.dementor.global.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.dementor.domain.admin.entity.Admin;
import com.dementor.domain.member.entity.Member;
import com.dementor.domain.member.entity.UserRole;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
	private final Long id;
	private final String email;
	private final String password;
	@Nullable
	private final String nickname;
	private final Collection<? extends GrantedAuthority> authorities;

	// Member 엔티티에서 CustomUserDetails 객체 생성
	public static CustomUserDetails of(Member member) {
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + member.getUserRole().name());

		return new CustomUserDetails(
			member.getId(),
			member.getEmail(),
			member.getPassword(),
			member.getNickname(),
			Collections.singleton(authority)
		);
	}

	// Member 엔티티에서 CustomUserDetails 객체 생성
	public static CustomUserDetails ofAdmin(Admin admin) {
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + "ADMIN");

		return new CustomUserDetails(
			admin.getId(),
			admin.getUsername(),
			admin.getPassword(),
			null,
			Collections.singleton(authority)
		);
	}

	// UserDetails 인터페이스 구현 메소드
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public UserRole getRole() {
		String roleStr = authorities.iterator().next().getAuthority().replace("ROLE_", "");
		return UserRole.valueOf(roleStr);
	}
}