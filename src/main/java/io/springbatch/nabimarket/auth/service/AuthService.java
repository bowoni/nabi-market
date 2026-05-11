package io.springbatch.nabimarket.auth.service;

import io.springbatch.nabimarket.auth.dto.*;
import io.springbatch.nabimarket.auth.jwt.JwtTokenProvider;
import io.springbatch.nabimarket.auth.oauth.OAuthSignupSession;
import io.springbatch.nabimarket.auth.repository.OAuthSignupSessionRepository;
import io.springbatch.nabimarket.auth.repository.RefreshTokenRepository;
import io.springbatch.nabimarket.auth.repository.VerificationCodeRepository;
import io.springbatch.nabimarket.auth.sms.SmsService;
import io.springbatch.nabimarket.global.exception.BusinessException;
import io.springbatch.nabimarket.global.exception.ErrorCode;
import io.springbatch.nabimarket.user.domain.Provider;
import io.springbatch.nabimarket.user.domain.User;
import io.springbatch.nabimarket.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuthSignupSessionRepository oauthSignupSessionRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final SmsService smsService;

    private static final SecureRandom secureRandom = new SecureRandom();

    private String generateVerificationCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

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

    public void sendOAuthVerificationCode(SendOAuthCodeRequest request) {
        // 1. tempToken 검증 + TTL 갱신
        if (!oauthSignupSessionRepository.refreshTtl(request.tempToken())) {
            throw new BusinessException(ErrorCode.EXPIRED_SIGNUP_SESSION);
        }
        // 2. 사전 중복 체크
        userRepository.findByPhoneNumber(request.phoneNumber()).ifPresent(existing -> {
            throw buildAlreadyRegisteredException(existing.getProvider());
        });
        // 3. 6자리 코드 생성 + Redis에 저장 + SMS 발송 (모킹)
        String code = generateVerificationCode();
        verificationCodeRepository.save(request.phoneNumber(), code);
        smsService.send(request.phoneNumber(), "[나비마켓] 인증번호: " + code);
    }

    @Transactional
    public TokenResponse verifyOAuthPhone(VerifyOAuthPhoneRequest request) {
        // 1. tempToken으로 OAuth 세션 조회
        OAuthSignupSession session = oauthSignupSessionRepository.findByTempToken(request.tempToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE));
        // 2. 인증 코드 검증
        String savedCode = verificationCodeRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE));
        if (!savedCode.equals(request.code())) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }
        // 3. 일회성 토큰 정리 (재사용 방지)
        verificationCodeRepository.deleteByPhoneNumber(request.phoneNumber());
        oauthSignupSessionRepository.deleteByTempToken(request.tempToken());
        // 4. 같은 phoneNumber로 기존 User 있으면 거부
        userRepository.findByPhoneNumber(request.phoneNumber())
                .ifPresent(existing -> {
                    throw buildAlreadyRegisteredException(existing.getProvider());
                });
        // 5. 새 user 생성
        User user = userRepository.save(buildOAuthUser(session, request.phoneNumber()));
        // 6. JWT 발급 + Redis 저장
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        refreshTokenRepository.save(user.getId(), refreshToken);

        return TokenResponse.of(accessToken, refreshToken);
    }

    private BusinessException buildAlreadyRegisteredException(Provider provider) {
        String message = switch (provider) {
            case LOCAL -> "이미 일반 회원으로 가입된 계정입니다. 아이디/비밀번호로 로그인해주세요.";
            case GOOGLE, KAKAO, NAVER ->
                    String.format("이미 %s로 가입된 계정입니다. %s 계정으로 로그인해주세요.",
                            provider.getDisplayName(), provider.getDisplayName());
        };
        return new
                BusinessException(ErrorCode.ALREADY_REGISTERED_WITH_DIFFERENT_PROVIDER,
                message);
    }

    private User buildOAuthUser(OAuthSignupSession session, String phoneNumber) {
        return User.builder()
                .loginId(generateOAuthLoginId(session))
                .nickname(generateUniqueNickname(session.name()))
                .phoneNumber(phoneNumber)
                .email(session.email())
                .provider(session.provider())
                .providerId(session.providerId())
                // password는 null (OAuth 사용자)
                .build();
    }

    private String generateOAuthLoginId(OAuthSignupSession session) {
        // "google_123" 형태. providerId가 길면 30자에 맞게 잘림 - 어차피 unique 보장됨
        String raw = session.provider().name().toLowerCase() + "_" + session.providerId();
        return raw.length() > 30 ? raw.substring(0, 30) : raw;
    }

    private String generateUniqueNickname(String baseName) {
        if (baseName == null || baseName.isBlank()) {
            baseName = "user";
        }
        // nickname 50자 제약 고려해 잘라둠
        if (baseName.length() > 45) {
            baseName = baseName.substring(0, 45);
        }

        String candidate = baseName;
        int suffix = 1;
        while (userRepository.existsByNickname(candidate)) {
            candidate = baseName + suffix++;
            if (suffix > 999) {
                // 극단적 케이스 - UUID 일부 붙임
                candidate = baseName + UUID.randomUUID().toString().substring(0, 6);
                break;
            }
        }
        return candidate;
    }

}
