package com.dementor.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtParser {

    // ★예시 시크릿키. 실제로 설정 파일에서 주입 받아야함
    private static final String SECRET_KEY = "your-secret-key-should-be-at-least-256-bits-long";

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    private Claims parseClaims(String token) {
        // "Bearer " 제거
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    public String getNickname(String token) {
        return parseClaims(token).get("nickname", String.class);
    }
}
