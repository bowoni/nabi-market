package io.springbatch.nabimarket.auth.service;

import io.springbatch.nabimarket.auth.dto.*;
import io.springbatch.nabimarket.auth.jwt.JwtTokenProvider;
import io.springbatch.nabimarket.auth.repository.RefreshTokenRepository;
import io.springbatch.nabimarket.global.exception.BusinessException;
import io.springbatch.nabimarket.global.exception.ErrorCode;
import io.springbatch.nabimarket.user.domain.Provider;
import io.springbatch.nabimarket.user.domain.User;
import io.springbatch.nabimarket.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        validateDuplicate(request);
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.builder()
                .loginId(request.loginId())
                .password(encodedPassword)
                .nickname(request.nickname())
                .phoneNumber(request.phoneNumber())
                .email(request.email())
                .provider(Provider.LOCAL)
                .build();
        User saved = userRepository.save(user);
        return SignupResponse.from(saved);
    }

    private void validateDuplicate(SignupRequest request) {
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        if (userRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new BusinessException(ErrorCode.DUPLICATE_PHONE_NUMBER);
        }
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        refreshTokenRepository.save(user.getId(), refreshToken);

        return TokenResponse.of(accessToken, refreshToken);
    }

    public TokenResponse refresh(RefreshRequest request) {
        String refreshToken = request.refreshToken();
        // 1. JWT 자체 검증 (서명, 만료)
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // 2. Redis에 저장된 토큰 조회
        String savedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        // 3. 일치 여부 검증 - 불일치면 모든 토큰 폐기
        if (!savedToken.equals(refreshToken)) {
            refreshTokenRepository.deleteByUserId(userId);
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 4. 새 토큰 발급 + Redis 덮어쓰기
        String newAccessToken = jwtTokenProvider.createAccessToken(userId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);
        refreshTokenRepository.save(userId, newRefreshToken);

        return TokenResponse.of(newAccessToken, newRefreshToken);
    }

    // Redis -> JPA 트랜잭션 영향 X
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

}
