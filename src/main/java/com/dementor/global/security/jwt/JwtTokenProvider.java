package com.dementor.global.security.jwt;

import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.dementor.domain.admin.entity.Admin;
import com.dementor.domain.member.entity.Member;
import com.dementor.global.security.CustomUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider implements InitializingBean {

	private static final String AUTHORITIES_KEY = "auth";

	@Value("${jwt.secret}")
	private final String secret;

	@Value("${jwt.expiration}")
	private final long tokenValidityInMilliseconds;

	private Key key;

	public JwtTokenProvider(
			@Value("${jwt.secret}") String secret,
			@Value("${jwt.expiration}") long tokenValidityInMilliseconds) {
		this.secret = secret;
		this.tokenValidityInMilliseconds = tokenValidityInMilliseconds;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		byte[] ketBytes = Decoders.BASE64.decode(secret);
		this.key = Keys.hmacShaKeyFor(ketBytes);
	}

	// Authentication에 권한 정보를 이요한 토큰 생성
	public String createMemberToken(Authentication authentication, Long memberId, String nickname) {
		String authorities = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(","));

		Map<String, Object> claims = new HashMap<>();
		claims.put(AUTHORITIES_KEY, authorities);
		claims.put("memberId", memberId);      // DB ID 추가
		claims.put("nickname", nickname);  // 닉네임 추가
		claims.put("sub", authentication.getName());

		long now = (new Date()).getTime();
		Date vaildity = new Date(now + tokenValidityInMilliseconds);

		return Jwts.builder()
			.setClaims(claims)
			.setIssuedAt(new Date(now))
			.signWith(key, SignatureAlgorithm.HS512)
			.setExpiration(vaildity)
			.compact();
	}

	// Admin 토큰 생성 메서드
	public String createAdminToken(Long adminId) {
		String authorities = "ROLE_ADMIN";

		Map<String, Object> claims = new HashMap<>();
		claims.put(AUTHORITIES_KEY, authorities);
		claims.put("adminId", adminId);
		claims.put("sub", "admin");

		long now = (new Date()).getTime();
		Date validity = new Date(now + tokenValidityInMilliseconds);

		return Jwts.builder()
			.setClaims(claims)
			.setIssuedAt(new Date(now))
			.signWith(key, SignatureAlgorithm.HS512)
			.setExpiration(validity)
			.compact();
	}

	//Token에 담겨있는 정보를 이용해 Authentication 객체 리턴
	public Authentication getAuthentication(String token) {
		Claims claims = Jwts
			.parserBuilder()
			.setSigningKey(key)
			.build()
			.parseClaimsJws(token)
			.getBody();

		Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
			new SimpleGrantedAuthority(claims.get(AUTHORITIES_KEY, String.class))
		);

		// role에 따라 Member 또는 Admin으로 구분
		String role = claims.get(AUTHORITIES_KEY, String.class);
		CustomUserDetails principal;

		if (role.startsWith("ROLE_ADMIN")) {
			principal = CustomUserDetails.ofAdmin(
				Admin.builder()
					.id(claims.get("adminId", Long.class))
					.username(claims.getSubject())
					.password("")  // JWT 토큰에는 password 저장하지 않음
					.build()
			);
		} else {
			principal = CustomUserDetails.of(
				Member.builder()
					.id(claims.get("memberId", Long.class))
					.email(claims.getSubject())
					.password("")  // JWT 토큰에는 password 저장하지 않음
					.nickname(claims.get("nickname", String.class))
					.build()
			);
		}

		return new UsernamePasswordAuthenticationToken(principal, token, authorities);
	}

	//토큰 유효성 검증
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		} catch (io.jsonwebtoken.security.SignatureException | MalformedJwtException e) {
			System.out.println("잘못된 JWT 서명"); //log 로 변경
		} catch (ExpiredJwtException e){
			System.out.println("만료된 JWT 토큰"); //log 로 변경
		} catch (UnsupportedJwtException e){
			System.out.println("지원되지 않는 JWT 토큰"); //log 로 변경
		}catch (IllegalArgumentException e){
			System.out.println("잘못된 JWT 토큰"); //log 로 변경
		}
		return false;
	}

}
