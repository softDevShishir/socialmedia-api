package com.shishir.socialmedia.user.controller;

import com.shishir.socialmedia.config.Routes;
import com.shishir.socialmedia.user.dto.UserProfileResponse;
import com.shishir.socialmedia.user.dto.UserUpdateRequest;
import com.shishir.socialmedia.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(name = "Users", description = "User profiles: view, update, follow/unfollow")
public class UserProfileController {

    private final UserService userService;

    @GetMapping(Routes.USERS)
    @Operation(summary = "Get all users", description = "Get list of all active users")
    @ApiResponse(responseCode = "200", description = "List of users")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping(Routes.USER_BY_USERNAME)
    @Operation(summary = "Get user by username", description = "Get public profile of user by username")
    @ApiResponse(responseCode = "200", description = "User profile found")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserProfileResponse> getUserByUsername(
            @Parameter(description = "Username of the user to retrieve") @PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @PutMapping(Routes.USER_BY_ID)
    @Operation(summary = "Update user profile", description = "Update authenticated user's profile")
    @ApiResponse(responseCode = "200", description = "Profile updated")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "403", description = "Cannot update another user's profile")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "409", description = "Username already taken")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Parameter(description = "ID of the user whose profile to update") @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request,
            Authentication authentication) {

        if (!userService.getCurrentUserId(authentication.getName()).equals(userId)) {
            throw new AccessDeniedException("You can only update your own profile");
        }

        return ResponseEntity.ok(userService.updateUserProfile(userId, request));
    }
}
