package io.springbatch.nabimarket.user.service;

import io.springbatch.nabimarket.global.exception.BusinessException;
import io.springbatch.nabimarket.global.exception.ErrorCode;
import io.springbatch.nabimarket.user.domain.Provider;
import io.springbatch.nabimarket.user.domain.User;
import io.springbatch.nabimarket.user.dto.UserResponse;
import io.springbatch.nabimarket.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("getMyInfo(): 사용자가 존재하면 UserResponse를 반환한다")
    void getMyInfo_withExistingUser_returnsUserResponse() {
        // given
        User user = User.builder()
                .loginId("testuser1")
                .password("encoded-password")
                .nickname("테스터")
                .phoneNumber("010-1234-5678")
                .email("test@example.com")
                .provider(Provider.LOCAL)
                .build();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when
        UserResponse response = userService.getMyInfo(1L);

        // then
        assertThat(response.loginId()).isEqualTo("testuser1");
        assertThat(response.nickname()).isEqualTo("테스터");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.provider()).isEqualTo(Provider.LOCAL);
    }

    @Test
    @DisplayName("getMyInfo(): 사용자가 없으면 USER_NOT_FOUND 예외 발생")
    void getMyInfo_withNonExistentUser_throwsException() {
        // given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getMyInfo(999L))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

}