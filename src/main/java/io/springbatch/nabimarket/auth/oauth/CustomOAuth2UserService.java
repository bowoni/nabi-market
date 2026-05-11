package io.springbatch.nabimarket.auth.oauth;

import io.springbatch.nabimarket.auth.repository.OAuthSignupSessionRepository;
import io.springbatch.nabimarket.user.domain.User;
import io.springbatch.nabimarket.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OAuthSignupSessionRepository signupSessionRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 부모 호출 - Spring Security가 OAuth 제공자에서 사용자 정보 가져옴
        OAuth2User oauth2User = super.loadUser(userRequest);

        // 2. registrationId(google/kakao/naver) + nameAttributeKey 추출
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String nameAttributeKey = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // 3. provider별 응답 파싱
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, oauth2User.getAttributes());

        // 4. DB에서 기존 사용자 조회 (provider + providerId)
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(attributes.provider(), attributes.providerId());

        if (existingUser.isPresent()) {
            // 기존 사용자: userId만 담음
            return new CustomOAuth2User(
                    Collections.emptyList(),
                    oauth2User.getAttributes(),
                    nameAttributeKey,
                    existingUser.get().getId(),
                    null
            );
        }

        // 5. 신규 사용자: Redis에 임시 저장 + tempToken 발급
        String tempToken = signupSessionRepository.save(new
                OAuthSignupSession(
                attributes.provider(),
                attributes.providerId(),
                attributes.email(),
                attributes.name()
        ));

        return new CustomOAuth2User(
                Collections.emptyList(),
                oauth2User.getAttributes(),
                nameAttributeKey,
                null,
                tempToken
        );
    }

}
