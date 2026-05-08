package io.springbatch.nabimarket.user.dto;

import io.springbatch.nabimarket.user.domain.Provider;
import io.springbatch.nabimarket.user.domain.Role;
import io.springbatch.nabimarket.user.domain.User;

public record UserResponse(
        Long id,
        String loginId,
        String nickname,
        String email,
        String phoneNumber,
        String profileImageUrl,
        boolean regionVerified,
        Role role,
        Provider provider
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getLoginId(),
                user.getNickname(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getProfileImageUrl(),
                user.isRegionVerified(),
                user.getRole(),
                user.getProvider()
        );
    }
}
