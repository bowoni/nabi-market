package io.springbatch.nabimarket.auth.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("createAccessToken(): userId로 Access Token을 생성하면 비어있지 않은 토큰이 반환된다")
    void createAccessToken() {
        // given
        Long userId = 1L;

        // when
        String token = jwtTokenProvider.createAccessToken(userId);

        // then
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);  // JWT는 점 3개로 구분
    }

    @Test
    @DisplayName("getUserIdFromToken(): 토큰에서 userId를 추출하면 원본 값과 일치한다")
    void getUserIdFromToken() {
        // given
        Long originalUserId = 42L;
        String token = jwtTokenProvider.createAccessToken(originalUserId);

        // when
        Long extracted = jwtTokenProvider.getUserIdFromToken(token);

        // then
        assertThat(extracted).isEqualTo(originalUserId);
    }

    @Test
    @DisplayName("validateToken(): 정상 토큰은 검증에 통과한다")
    void validateToken_validToken() {
        String token = jwtTokenProvider.createAccessToken(1L);
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateToken(): 잘못된 형식의 토큰은 검증에 실패한다")
    void validateToken_invalidToken() {
        assertThat(jwtTokenProvider.validateToken("not-a-jwt")).isFalse();
    }
}