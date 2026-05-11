package io.springbatch.nabimarket.user.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordCheckRequest(
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {
}
