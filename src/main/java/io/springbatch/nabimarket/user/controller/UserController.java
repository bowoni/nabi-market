package io.springbatch.nabimarket.user.controller;

import io.springbatch.nabimarket.auth.dto.TokenResponse;
import io.springbatch.nabimarket.region.dto.UpdateRegionRequest;
import io.springbatch.nabimarket.user.dto.*;
import io.springbatch.nabimarket.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getMyInfo(
            @AuthenticationPrincipal Long userId
    ) {
        UserResponse response = userService.getMyInfo(userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateMyInfo(
            @Valid @RequestBody UpdateMyInfoRequest request, @AuthenticationPrincipal Long userId
    ) {
        UserResponse response = userService.updateMyInfo(userId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TokenResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request, @AuthenticationPrincipal Long userId
    ) {
        TokenResponse response = userService.changePassword(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteMyAccount(
            @RequestBody DeleteAccountRequest request, @AuthenticationPrincipal Long userId
    ) {
        userService.deleteMyAccount(userId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/password/check-current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PasswordCheckResponse> checkCurrentPassword(
            @Valid @RequestBody PasswordCheckRequest request, @AuthenticationPrincipal Long userId
    ) {
        boolean matches = userService.verifyCurrentPassword(userId, request.password());
        return ResponseEntity.ok(new PasswordCheckResponse(matches));
    }

    @PostMapping("/me/password/check-new")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PasswordCheckResponse> checkNewPassword(
            @Valid @RequestBody PasswordCheckRequest request, @AuthenticationPrincipal Long userId
    ) {
        boolean usable = !userService.isNewPasswordSameAsCurrent(userId, request.password());
        return ResponseEntity.ok(new PasswordCheckResponse(usable));
    }

    @PatchMapping("/me/region")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateMyRegion(
            @Valid @RequestBody UpdateRegionRequest request, @AuthenticationPrincipal Long userId
    ) {
        userService.updateRegion(userId, request.regionId());
        return ResponseEntity.noContent().build();
    }

}
