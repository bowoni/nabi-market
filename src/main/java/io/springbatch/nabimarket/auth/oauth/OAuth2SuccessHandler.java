package io.springbatch.nabimarket.auth.oauth;

import io.springbatch.nabimarket.auth.jwt.JwtTokenProvider;
import io.springbatch.nabimarket.auth.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication
    ) throws IOException {
        CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();

        String redirectUrl = oauth2User.isNewUser()
                ? buildPhoneVerificationUrl(oauth2User.getTempToken())
                : buildSuccessUrl(oauth2User.getUserId());

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    // 신규 사용자 → 폰번호 인증 페이지로 (tempToken 전달)
    // TODO: Access/Refresh 토큰을 HttpOnly Secure 쿠키로 전달 변경 - query param은 브라우저 history/서버 로그에 남아 토큰 노출 위험
    private String buildPhoneVerificationUrl(String tempToken) {
        return UriComponentsBuilder.fromUriString(frontendUrl + "/oauth/phone-verification")
                .queryParam("tempToken", tempToken)
                .build()
                .toUriString();
    }

    // 기존 사용자 → JWT 발급 후 성공 페이지로 (토큰 전달)
    private String buildSuccessUrl(Long userId) {
        String accessToken = jwtTokenProvider.createAccessToken(userId);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);
        refreshTokenRepository.save(userId, refreshToken);

        return UriComponentsBuilder.fromUriString(frontendUrl + "/oauth/success")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();
    }

}
