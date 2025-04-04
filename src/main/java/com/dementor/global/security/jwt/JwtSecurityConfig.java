package com.dementor.global.security.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

	private final JwtTokenProvider jwtTokenProvider;

	@Value("${jwt.cookie.name}")
	private String cookieName;

	// JWT 필터를 시큐리티 필터 체인에 추가
	@Override
	public void configure(HttpSecurity http) {
		JwtAuthenticationFilter customFilter = new JwtAuthenticationFilter(jwtTokenProvider, cookieName);
		http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);

	}
}
