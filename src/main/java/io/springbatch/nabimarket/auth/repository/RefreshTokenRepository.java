package io.springbatch.nabimarket.auth.repository;

import io.springbatch.nabimarket.auth.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh:";

    // JSON 객체 등을 저장하려면 StringRedisTemplate -> RedisTemplate<String, Object>
    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    public void save(Long userId, String refreshToken) {
        Duration ttl = Duration.ofMillis(jwtProperties.getRefreshTokenExpiration());
        // Redis 키 자체에 TTL을 거는 거, 만료된 토큰은 Redis가 자동 삭제 → 메모리도 깨끗하게 유지, JWT 만료시간과 일치시켜야 양쪽이 동시에 만료됨.
        redisTemplate.opsForValue().set(buildKey(userId), refreshToken, ttl);
    }

    public Optional<String> findByUserId(Long userId) {
        String value = redisTemplate.opsForValue().get(buildKey(userId));
        return Optional.ofNullable(value);
    }

    public void deleteByUserId(Long userId) {
        redisTemplate.delete(buildKey(userId));
    }

    private String buildKey(Long userId) {
        return KEY_PREFIX + userId;
    }

}
