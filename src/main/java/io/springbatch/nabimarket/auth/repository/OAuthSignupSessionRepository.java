package io.springbatch.nabimarket.auth.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.springbatch.nabimarket.auth.oauth.OAuthSignupSession;
import io.springbatch.nabimarket.global.exception.BusinessException;
import io.springbatch.nabimarket.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OAuthSignupSessionRepository {

    private static final String KEY_PREFIX = "oauth-signup:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final StringRedisTemplate redisTemplate;

    public String save(OAuthSignupSession session) {
        String tempToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(buildKey(tempToken), toJson(session), TTL);
        return tempToken;
    }

    public Optional<OAuthSignupSession> findByTempToken(String tempToken) {
        String json = redisTemplate.opsForValue().get(buildKey(tempToken));
        if (json == null) {
            return Optional.empty();
        }
        return Optional.of(fromJson(json));
    }

    public void deleteByTempToken(String tempToken) {
        redisTemplate.delete(buildKey(tempToken));
    }

    private String buildKey(String tempToken) {
        return KEY_PREFIX + tempToken;
    }

    private String toJson(OAuthSignupSession session) {
        try {
            return objectMapper.writeValueAsString(session);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private OAuthSignupSession fromJson(String json) {
        try {
            return objectMapper.readValue(json, OAuthSignupSession.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

}
