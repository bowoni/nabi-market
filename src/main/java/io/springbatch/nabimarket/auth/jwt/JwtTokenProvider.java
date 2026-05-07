package io.springbatch.nabimarket.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    // SecretKey 한 번만 생성
    @PostConstruct
    void init() {
        secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    // Access Token 발급 (짧은 만료)
    public String createAccessToken(Long userId) {
        return createToken(userId, jwtProperties.getAccessTokenExpiration());
    }

    // Refresh Token 발급 (긴 만료)
    public String createRefreshToken(Long userId) {
        return createToken(userId, jwtProperties.getRefreshTokenExpiration());
    }

    // JWT 토큰 생성 메서드
    private String createToken(Long userId, long expirationMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    // 토큰에서 사용자 ID 추출
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    // JWT 토큰 유효성 검증 메서드
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            // TODO: 만료/서명오류/형식오류 분리 처리는 필터 단계에서
            return false;
        }
    }

    // JWT 토큰을 받아서 서명을 검증하고, payload(Claims) 부분만 꺼내서 반환
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
