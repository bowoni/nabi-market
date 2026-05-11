package io.springbatch.nabimarket.user.service;

import io.springbatch.nabimarket.auth.dto.TokenResponse;
import io.springbatch.nabimarket.auth.jwt.JwtTokenProvider;
import io.springbatch.nabimarket.auth.repository.RefreshTokenRepository;
import io.springbatch.nabimarket.global.exception.BusinessException;
import io.springbatch.nabimarket.global.exception.ErrorCode;
import io.springbatch.nabimarket.user.domain.Provider;
import io.springbatch.nabimarket.user.domain.User;
import io.springbatch.nabimarket.user.dto.ChangePasswordRequest;
import io.springbatch.nabimarket.user.dto.DeleteAccountRequest;
import io.springbatch.nabimarket.user.dto.UpdateMyInfoRequest;
import io.springbatch.nabimarket.user.dto.UserResponse;
import io.springbatch.nabimarket.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateMyInfo(Long userId, UpdateMyInfoRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (request.nickname() != null && userRepository.existsByNicknameAndIdNot(request.nickname(), userId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
        user.updateMyInfo(request.nickname(), request.email());
        return UserResponse.from(user);
    }

    @Transactional
    public TokenResponse changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        // 1. OAuth 가입자 거부
        if (user.getProvider() != Provider.LOCAL) {
            throw new BusinessException(ErrorCode.PASSWORD_CHANGE_NOT_ALLOWED);
        }
        // 2. 현재 비밀번호 검증
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CURRENT_PASSWORD);
        }
        // 3. 새 비밀번호가 현재와 동일한지 검증
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.SAME_AS_CURRENT_PASSWORD);
        }
        // 4. 비밀번호 변경
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        // 5. 다른 세션 끊기 + 본인 새 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(userId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);
        refreshTokenRepository.save(userId, newRefreshToken);

        return TokenResponse.of(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void deleteMyAccount(Long userId, DeleteAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        verifyCurrentPasswordIfLocal(user, request.currentPassword());
        user.markDeleted();
        refreshTokenRepository.deleteByUserId(userId);
    }

    private void verifyCurrentPasswordIfLocal(User user, String currentPassword) {
        if (user.getProvider() != Provider.LOCAL) {
            return;
        }
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CURRENT_PASSWORD);
        }
    }

    public boolean verifyCurrentPassword(Long userId, String currentPassword) {
        User user = getLocalUserOrThrow(userId);
        return passwordEncoder.matches(currentPassword, user.getPassword());
    }

    public boolean isNewPasswordSameAsCurrent(Long userId, String newPassword) {
        User user = getLocalUserOrThrow(userId);
        return passwordEncoder.matches(newPassword, user.getPassword());
    }

    private User getLocalUserOrThrow(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getProvider() != Provider.LOCAL) {
            throw new BusinessException(ErrorCode.PASSWORD_CHANGE_NOT_ALLOWED);
        }
        return user;
    }

}
