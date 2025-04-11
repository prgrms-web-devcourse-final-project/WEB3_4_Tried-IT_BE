package com.dementor.global.security.jwt.repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RedisRefreshTokenRepository implements RefreshTokenRepository {

	private final RedisTemplate<String, String> redisTemplate;
	private static final String KEY_PREFIX = "refresh:";

	@Value("${jwt.secret}")
	private String secret;

	@Override
	public void save(String userIdentifier, String refreshToken, long expiration) {
		String key = KEY_PREFIX + userIdentifier;
		try {
			redisTemplate.opsForValue().set(key, refreshToken);
			redisTemplate.expire(key, expiration, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			throw new RuntimeException("Failed to save refresh token", e);
		}
	}

	@Override
	public Optional<String> findByUserIdentifier(String userIdentifier) {
		try {
			String key = KEY_PREFIX + userIdentifier;
			String value = redisTemplate.opsForValue().get(key);
			return Optional.ofNullable(value);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	@Override
	public boolean validateRefreshToken(String refreshToken) {

		// 1️⃣ JWT 검증 (서명 확인)
		Claims claims = Jwts.parserBuilder()
			.setSigningKey(secret)
			.build()
			.parseClaimsJws(refreshToken)
			.getBody();

		String userIdentifier = claims.getSubject();
		String key = KEY_PREFIX + userIdentifier;

		try {
			String storedToken = redisTemplate.opsForValue().get(key);
			return refreshToken.equals(storedToken); // 저장된 값과 비교
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void deleteByUserIdentifier(String userIdentifier) {
		try {
			String key = KEY_PREFIX + userIdentifier;
			// 토큰 값 가져오기
			String refreshToken = redisTemplate.opsForValue().get(key);

			if (refreshToken != null) {
				redisTemplate.delete(refreshToken);
			}

			redisTemplate.delete(key);

		} catch (Exception e) {
			return;
		}
	}

}
