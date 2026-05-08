package io.springbatch.nabimarket.auth.service;

import io.springbatch.nabimarket.auth.dto.LoginRequest;
import io.springbatch.nabimarket.auth.dto.SignupRequest;
import io.springbatch.nabimarket.auth.dto.SignupResponse;
import io.springbatch.nabimarket.auth.dto.TokenResponse;
import io.springbatch.nabimarket.auth.jwt.JwtTokenProvider;
import io.springbatch.nabimarket.global.exception.BusinessException;
import io.springbatch.nabimarket.global.exception.ErrorCode;
import io.springbatch.nabimarket.user.domain.Provider;
import io.springbatch.nabimarket.user.domain.User;
import io.springbatch.nabimarket.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private SignupRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new SignupRequest(
                "testuser1",
                "test1234",
                "테스터",
                "010-1234-5678",
                "test@test.com"
        );
    }

    @Test
    @DisplayName("signup(): 모든 검증 통과 시 사용자 저장 및 SignupResponse 반환")
    void signup_withValidInput_savesAndReturnsResponse() {
        // given - 모든 중복검증을 false로
        // given().willReturn(): any()-> 어떤 인자가 와도 willReturn-> 이걸 반환해라
        given(userRepository.existsByLoginId(any())).willReturn(false);
        given(userRepository.existsByNickname(any())).willReturn(false);
        given(userRepository.existsByPhoneNumber(any())).willReturn(false);

        given(passwordEncoder.encode("test1234")).willReturn("encoded-password");

        User savedUser = User.builder()
                .loginId(validRequest.loginId())
                .password("encoded-password")
                .nickname(validRequest.nickname())
                .phoneNumber(validRequest.phoneNumber())
                .email(validRequest.email())
                .provider(Provider.LOCAL)
                .build();
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // when
        SignupResponse response = authService.signup(validRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.loginId()).isEqualTo("testuser1");
        assertThat(response.nickname()).isEqualTo("테스터");
        // verify() — 호출 검증
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("signup(): 비밀번호는 BCrypt로 암호화되어 저장된다")
    void signup_withValidInput_encodesPasswordBeforeSave() {
        // given
        given(userRepository.existsByLoginId(any())).willReturn(false);
        given(userRepository.existsByNickname(any())).willReturn(false);
        given(userRepository.existsByPhoneNumber(any())).willReturn(false);

        given(passwordEncoder.encode("test1234")).willReturn("encoded-password");
        // willAnswer() 전달받은 첫 번째 인자(User)를 그대로 반환
        given(userRepository.save(any(User.class))).willAnswer(invocation ->
                invocation.getArgument(0));

        // when
        authService.signup(validRequest);

        // then - save에 넘어간 User의 password가 인코딩된 값인지 검증
        // ArgumentCaptor — 전달된 인자 캡처
        // save()에 어떤 User 객체가 넘어갔는지 캡처해서 그 내부 필드를 검증할 때 사용
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encoded-password");

        assertThat(userCaptor.getValue().getProvider()).isEqualTo(Provider.LOCAL);
    }

    @Test
    @DisplayName("signup(): loginId 중복 시 IllegalArgumentException 발생")
    void signup_withDuplicateLoginId_throwsException() {
        // given
        given(userRepository.existsByLoginId("testuser1")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signup(validRequest))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_LOGIN_ID));

        // save가 호출되지 않았는지 검증
        verify(userRepository, org.mockito.Mockito.never()).save(any(User.class));
    }

    @Test
    @DisplayName("signup(): nickname 중복 시 IllegalArgumentException 발생")
    void signup_withDuplicateNickname_throwsException() {
        // given
        given(userRepository.existsByLoginId(any())).willReturn(false);
        given(userRepository.existsByNickname("테스터")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signup(validRequest))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_NICKNAME));
    }

    @Test
    @DisplayName("signup(): phoneNumber 중복 시 IllegalArgumentException 발생")
    void signup_withDuplicatePhoneNumber_throwsException() {
        // given
        given(userRepository.existsByLoginId(any())).willReturn(false);
        given(userRepository.existsByNickname(any())).willReturn(false);

        given(userRepository.existsByPhoneNumber("010-1234-5678")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signup(validRequest))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_PHONE_NUMBER));
    }

    @Test
    @DisplayName("login(): 정상 입력 시 access/refresh 토큰을 발급하고 TokenResponse 반환")
    void login_withValidInput_returnsTokenResponse() {
        // given
        LoginRequest request = new LoginRequest("testuser1", "test1234");
        User user = User.builder()
                .loginId("testuser1")
                .password("encoded-password")
                .nickname("테스터")
                .phoneNumber("010-1234-5678")
                .email("test@example.com")
                .provider(Provider.LOCAL)
                .build();

        given(userRepository.findByLoginId("testuser1")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("test1234",
                "encoded-password")).willReturn(true);

        given(jwtTokenProvider.createAccessToken(any())).willReturn("access-token");

        given(jwtTokenProvider.createRefreshToken(any())).willReturn("refresh-token");

        // when
        TokenResponse response = authService.login(request);

        // then
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("login(): 존재하지 않는 loginId면 IllegalArgumentException 발생")
    void login_withNonExistentLoginId_throwsException() {
        // given
        LoginRequest request = new LoginRequest("no-such-user", "any1234");
        given(userRepository.findByLoginId("no-such-user")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    @Test
    @DisplayName("login(): 비밀번호 불일치 시 IllegalArgumentException 발생")
    void login_withWrongPassword_throwsException() {
        // given
        LoginRequest request = new LoginRequest("testuser1", "wrong1234");
        User user = User.builder()
                .loginId("testuser1")
                .password("encoded-password")
                .nickname("테스터")
                .phoneNumber("010-1234-5678")
                .provider(Provider.LOCAL)
                .build();

        given(userRepository.findByLoginId("testuser1")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong1234",
                "encoded-password")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    @Test
    @DisplayName("login(): 사용자 없음과 비번 불일치는 동일 메시지여야 함 (계정 enumeration 방지)")
    void login_failureMessages_areIdentical() {
        // given - 사용자 없음 케이스
        LoginRequest noUserRequest = new LoginRequest("no-such-user", "any1234");
        given(userRepository.findByLoginId("no-such-user")).willReturn(Optional.empty());

        String noUserMessage = catchThrowable(() ->
                authService.login(noUserRequest)).getMessage();

        // given - 비번 틀림 케이스
        LoginRequest wrongPwRequest = new LoginRequest("testuser1", "wrong1234");
        User user = User.builder()
                .loginId("testuser1")
                .password("encoded-password")
                .nickname("테스터")
                .phoneNumber("010-1234-5678")
                .provider(Provider.LOCAL)
                .build();
        given(userRepository.findByLoginId("testuser1")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong1234",
                "encoded-password")).willReturn(false);

        String wrongPwMessage = catchThrowable(() ->
                authService.login(wrongPwRequest)).getMessage();

        // then
        assertThat(noUserMessage).isEqualTo(wrongPwMessage);
    }

}