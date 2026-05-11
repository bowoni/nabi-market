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
            case KAKAO -> ofKakao(attributes);
            case NAVER -> ofNaver(attributes);
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

    // Map<String, Object> → 중첩 Map<String, Object> 캐스팅 시 컴파일러 경고. 어쩔 수 없는 상황(JSON 응답이 원래 unchecked Map)이라 무시
    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofKakao(Map<String, Object> attributes) {
        String providerId = String.valueOf(attributes.get("id"));
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = kakaoAccount == null ? null : (Map<String, Object>) kakaoAccount.get("profile");
        String email = kakaoAccount == null ? null : (String) kakaoAccount.get("email");
        String nickname = profile == null ? null : (String) profile.get("nickname");
        return new OAuthAttributes(Provider.KAKAO, providerId, email, nickname);
    }

    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofNaver(Map<String, Object> attributes) {
        // 네이버는 모든 사용자 정보가 response 키 아래로 한 번 감싸져 있음
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        return new OAuthAttributes(
                Provider.NAVER,
                (String) response.get("id"),
                (String) response.get("email"),
                (String) response.get("name")
        );
    }
}
