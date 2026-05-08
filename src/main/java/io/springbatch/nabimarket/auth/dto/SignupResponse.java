package io.springbatch.nabimarket.auth.dto;

import io.springbatch.nabimarket.user.domain.User;

public record SignupResponse(
        Long id,
        String loginId,
        String nickname
) {
    public static SignupResponse from(User user) {
        return new SignupResponse(user.getId(), user.getLoginId(), user.getNickname());
    }
}
