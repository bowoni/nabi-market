package io.springbatch.nabimarket.user.controller;

import io.springbatch.nabimarket.user.dto.UserResponse;
import io.springbatch.nabimarket.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(
            @AuthenticationPrincipal Long userId
    ) {
        UserResponse response = userService.getMyInfo(userId);
        return ResponseEntity.ok(response);
    }
}
