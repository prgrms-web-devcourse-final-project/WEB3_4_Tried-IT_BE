package com.dementor.global.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.dementor.global.security.jwt.JwtAccessDeniedHandler;
import com.dementor.global.security.jwt.JwtAuthenticationEntryPoint;
import com.dementor.global.security.jwt.JwtAuthenticationFilter;
import com.dementor.global.security.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
@Profile("!test") // 테스트 프로필이 아닐 때만 적용 (없으면 TestSecurityConfig 랑 충돌남)
public class SecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

	// 쿠키 이름을 위한 값 추가
	@Value("${jwt.cookie.name}")
	private String cookieName;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)

			.exceptionHandling(exceptionHandling -> exceptionHandling
				.accessDeniedHandler(jwtAccessDeniedHandler)
				.authenticationEntryPoint(jwtAuthenticationEntryPoint)
			)

			.authorizeHttpRequests(authorizeRequests -> authorizeRequests
				.requestMatchers("/api/signup/**").permitAll()
				.requestMatchers("/api/member/login").permitAll()
				.requestMatchers("/api/authenticate").permitAll()
				.requestMatchers("/swagger-ui").permitAll()
				.requestMatchers("/actuator/**").permitAll()
				.anyRequest().authenticated()
			)

			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

			.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, cookieName), UsernamePasswordAuthenticationFilter.class);
		;
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}
}
