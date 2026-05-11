package io.springbatch.nabimarket.user.dto;

public record DeleteAccountRequest(
        String currentPassword
) {
}
