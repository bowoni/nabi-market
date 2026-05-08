package io.springbatch.nabimarket.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "refresh token은 필수입니다.")
        String refreshToken
) {
}
