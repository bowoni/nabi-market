package io.springbatch.nabimarket.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VerificationCodeRepository {

    private static final String KEY_PREFIX = "verification:";
    private static final Duration TTL = Duration.ofMinutes(5);  // 인증 코드 유효 시간 5분

    private final StringRedisTemplate redisTemplate;

    public void save(String phoneNumber, String code) {
        redisTemplate.opsForValue().set(buildKey(phoneNumber), code, TTL);
    }

    public Optional<String> findByPhoneNumber(String phoneNumber) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(buildKey(phoneNumber)));
    }

    public void deleteByPhoneNumber(String phoneNumber) {
        redisTemplate.delete(buildKey(phoneNumber));
    }

    private String buildKey(String phoneNumber) {
        return KEY_PREFIX + phoneNumber;
    }

}
