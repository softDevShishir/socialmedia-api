package com.shishir.socialmedia.follow.controller;

import com.shishir.socialmedia.config.Routes;
import com.shishir.socialmedia.follow.dto.FollowActionResponse;
import com.shishir.socialmedia.follow.dto.FollowCheckResponse;
import com.shishir.socialmedia.follow.dto.UserFollowResponse;
import com.shishir.socialmedia.follow.service.FollowService;
import com.shishir.socialmedia.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Follow", description = "User follow/unfollow management")
public class FollowController {

    private final FollowService followService;
    private final UserService userService;

    @PostMapping(Routes.FOLLOW_USER)
    @Operation(summary = "Follow user", description = "Follow a user")
    public ResponseEntity<FollowActionResponse> followUser(@PathVariable Long userId, Authentication authentication) {
        Long followerId = userService.getCurrentUserId(authentication.getName());
        FollowActionResponse response = followService.followUser(followerId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping(Routes.UNFOLLOW_USER)
    @Operation(summary = "Unfollow user", description = "Unfollow a user")
    public ResponseEntity<FollowActionResponse> unfollowUser(@PathVariable Long userId, Authentication authentication) {
        Long followerId = userService.getCurrentUserId(authentication.getName());
        return ResponseEntity.ok(followService.unfollowUser(followerId, userId));
    }

    @GetMapping(Routes.USER_FOLLOWERS)
    @Operation(summary = "Get user followers", description = "Get list of users following a specific user")
    public ResponseEntity<List<UserFollowResponse>> getUserFollowers(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getUserFollowers(userId));
    }

    @GetMapping(Routes.USER_FOLLOWING)
    @Operation(summary = "Get user following", description = "Get list of users that a specific user is following")
    public ResponseEntity<List<UserFollowResponse>> getUserFollowing(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getUserFollowing(userId));
    }

    @GetMapping(Routes.FOLLOW_CHECK)
    @Operation(summary = "Check if following", description = "Check if authenticated user is following a specific user")
    public ResponseEntity<FollowCheckResponse> checkIfFollowing(@PathVariable Long userId, Authentication authentication) {
        Long followerId = userService.getCurrentUserId(authentication.getName());
        return ResponseEntity.ok(followService.checkIfFollowing(followerId, userId));
    }
}
