package com.dementor.global.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
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
import org.springframework.web.cors.CorsUtils;

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

				.requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

				.requestMatchers("/api/admin/refresh").permitAll()
				.requestMatchers("/api/members/refresh").permitAll()

				.requestMatchers("/api/members/info").hasAnyRole("MENTOR", "MENTEE")
				.requestMatchers("/api/members/logout").authenticated()

				//내 정보, 로그아웃 제외 허용
				.requestMatchers("/api/members/**").permitAll()

				.requestMatchers("/api/admin/login").permitAll()

				//관리자 로그인제외 권한
				.requestMatchers("/api/admin/logout").authenticated()
				.requestMatchers("/api/admin/**").hasRole("ADMIN")

				.requestMatchers(HttpMethod.GET, "/api/class").permitAll() // 모든 수업 조회 허용
				.requestMatchers(HttpMethod.GET, "/api/class/{classId}").permitAll() // 특정 수업 조회 허용

				.requestMatchers("/api/authenticate").permitAll()
				.requestMatchers("/v3/api-docs/**").permitAll() // swagger 문서 허용
				.requestMatchers("/swagger-ui/**").permitAll() // swagger 주소 허용
				.requestMatchers("/actuator/**").permitAll()
				.requestMatchers("/").permitAll()
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
