package io.springbatch.nabimarket.auth.oauth;

import io.springbatch.nabimarket.global.exception.BusinessException;
import io.springbatch.nabimarket.global.exception.ErrorCode;
import io.springbatch.nabimarket.user.domain.Provider;

import java.util.Map;

public record OAuthAttributes(
        Provider provider,
        String providerId,
        String email,
        String name
) {
    public static OAuthAttributes of(String registrationId, Map<String,
            Object> attributes) {
        Provider provider = Provider.valueOf(registrationId.toUpperCase());
        return switch (provider) {
            case GOOGLE -> ofGoogle(attributes);
            // TODO: case KAKAO -> ofKakao(attributes);
            // TODO: case NAVER -> ofNaver(attributes);
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT);
        };
    }

    private static OAuthAttributes ofGoogle(Map<String, Object> attributes) {
        return new OAuthAttributes(
                Provider.GOOGLE,
                (String) attributes.get("sub"),
                (String) attributes.get("email"),
                (String) attributes.get("name")
        );
    }
}
