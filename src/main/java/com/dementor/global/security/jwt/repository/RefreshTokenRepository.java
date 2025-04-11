package com.dementor.global.security.jwt.repository;

import java.util.Optional;

public interface RefreshTokenRepository {
	void save(String userIdentifier, String refreshToken, long expiration);

	Optional<String> findByUserIdentifier(String userIdentifier);

	boolean validateRefreshToken(String refreshToken);

	void deleteByUserIdentifier(String userIdentifier);
}
