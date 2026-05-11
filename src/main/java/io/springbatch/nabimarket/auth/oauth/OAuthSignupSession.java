package io.springbatch.nabimarket.auth.oauth;

import io.springbatch.nabimarket.user.domain.Provider;

public record OAuthSignupSession(
        Provider provider,
        String providerId,
        String email,
        String name
) {
}
