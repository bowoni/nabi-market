package io.springbatch.nabimarket.auth.service;

import io.springbatch.nabimarket.auth.dto.LoginRequest;
import io.springbatch.nabimarket.auth.dto.SignupRequest;
import io.springbatch.nabimarket.auth.dto.SignupResponse;
import io.springbatch.nabimarket.auth.dto.TokenResponse;
import io.springbatch.nabimarket.auth.jwt.JwtTokenProvider;
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

        return TokenResponse.of(accessToken, refreshToken);
    }

}
