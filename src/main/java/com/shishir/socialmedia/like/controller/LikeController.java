package com.shishir.socialmedia.like.controller;

import com.shishir.socialmedia.config.Routes;
import com.shishir.socialmedia.like.dto.LikeCheckResponse;
import com.shishir.socialmedia.like.dto.LikeResponse;
import com.shishir.socialmedia.like.service.LikeService;
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
@Tag(name = "Likes", description = "Like management on posts")
public class LikeController {

    private final LikeService likeService;
    private final UserService userService;

    @PostMapping(Routes.POST_LIKES)
    @Operation(summary = "Like post", description = "Like a post")
    public ResponseEntity<LikeResponse> likePost(@PathVariable Long postId, Authentication authentication) {
        Long userId = userService.getCurrentUserId(authentication.getName());
        LikeResponse response = likeService.likePost(postId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping(Routes.POST_LIKES)
    @Operation(summary = "Unlike post", description = "Unlike a post")
    public ResponseEntity<Void> unlikePost(@PathVariable Long postId, Authentication authentication) {
        Long userId = userService.getCurrentUserId(authentication.getName());
        likeService.unlikePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(Routes.POST_LIKES)
    @Operation(summary = "Get post likes", description = "Get all users who liked a post")
    public ResponseEntity<List<LikeResponse>> getPostLikes(@PathVariable Long postId) {
        return ResponseEntity.ok(likeService.getPostLikes(postId));
    }

    @GetMapping(Routes.POST_LIKE_CHECK)
    @Operation(summary = "Check if user liked post", description = "Check if authenticated user liked a specific post")
    public ResponseEntity<LikeCheckResponse> checkIfUserLikedPost(@PathVariable Long postId, Authentication authentication) {
        Long userId = userService.getCurrentUserId(authentication.getName());
        return ResponseEntity.ok(likeService.checkIfUserLikedPost(postId, userId));
    }
}
