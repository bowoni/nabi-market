package io.springbatch.nabimarket.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateMyInfoRequest(
        @Size(min = 2, max = 50, message = "닉네임은 2~50자입니다.")
        String nickname,

        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 255, message = "이메일은 255자 이하입니다.")
        String email
) {
}
