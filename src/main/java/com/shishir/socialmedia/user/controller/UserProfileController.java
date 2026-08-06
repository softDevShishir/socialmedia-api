package com.shishir.socialmedia.user.controller;

import com.shishir.socialmedia.config.Routes;
import com.shishir.socialmedia.user.dto.UserProfileResponse;
import com.shishir.socialmedia.user.dto.UserUpdateRequest;
import com.shishir.socialmedia.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "User Profiles", description = "Public user profile management")
public class UserProfileController {

    private final UserService userService;

    @GetMapping(Routes.USERS)
    @Operation(summary = "Get all users", description = "Get list of all active users")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping(Routes.USER_BY_USERNAME)
    @Operation(summary = "Get user by username", description = "Get public profile of a user by username")
    public ResponseEntity<UserProfileResponse> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @PutMapping(Routes.USER_BY_ID)
    @Operation(summary = "Update user profile", description = "Update authenticated user's profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request,
            Authentication authentication) {

        UserProfileResponse currentUser = userService.getCurrentUser(authentication.getName());
        if (!currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You can only update your own profile");
        }

        return ResponseEntity.ok(userService.updateUserProfile(userId, request));
    }
}
