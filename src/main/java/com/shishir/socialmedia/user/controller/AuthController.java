package com.shishir.socialmedia.user.controller;

import com.shishir.socialmedia.config.Routes;
import com.shishir.socialmedia.user.dto.LoginResponse;
import com.shishir.socialmedia.user.dto.UserLoginRequest;
import com.shishir.socialmedia.user.dto.UserProfileResponse;
import com.shishir.socialmedia.user.dto.UserRegisterRequest;
import com.shishir.socialmedia.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and login")
public class AuthController {

    private final UserService userService;

    @PostMapping(Routes.AUTH_REGISTER)
    @Operation(summary = "Register new user", description = "Create a new user account")
    public ResponseEntity<UserProfileResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        UserProfileResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(Routes.AUTH_LOGIN)
    @Operation(summary = "User login", description = "Authenticate user and receive JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        LoginResponse response = userService.loginUser(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping(Routes.AUTH_ME)
    @Operation(summary = "Get current user", description = "Get authenticated user profile")
    public ResponseEntity<UserProfileResponse> getCurrentUser(Authentication authentication) {
        UserProfileResponse response = userService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(response);
    }
}
