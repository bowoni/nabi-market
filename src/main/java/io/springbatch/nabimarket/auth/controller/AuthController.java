package io.springbatch.nabimarket.auth.controller;

import io.springbatch.nabimarket.auth.dto.*;
import io.springbatch.nabimarket.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SignupResponse> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody RefreshRequest request
    ) {
        TokenResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Long userId) {
        authService.logout(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/oauth/phone/send-code")
    public ResponseEntity<Void> sendOAuthVerificationCode(
            @Valid @RequestBody SendOAuthCodeRequest request
    ) {
        authService.sendOAuthVerificationCode(request);
        return ResponseEntity.noContent().build();   // 204
    }

    @PostMapping("/oauth/phone/verify")
    public ResponseEntity<TokenResponse> verifyOAuthPhone(
            @Valid @RequestBody VerifyOAuthPhoneRequest request
    ) {
        TokenResponse response = authService.verifyOAuthPhone(request);
        return ResponseEntity.ok(response);
    }

}
