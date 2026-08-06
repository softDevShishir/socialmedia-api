package com.shishir.socialmedia.post.controller;

import com.shishir.socialmedia.config.Routes;
import com.shishir.socialmedia.post.dto.CreatePostRequest;
import com.shishir.socialmedia.post.dto.PostDetailResponse;
import com.shishir.socialmedia.post.dto.PostResponse;
import com.shishir.socialmedia.post.dto.UpdatePostRequest;
import com.shishir.socialmedia.post.service.PostService;
import com.shishir.socialmedia.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Post creation and management")
public class PostController {

    private final PostService postService;
    private final UserService userService;

    @PostMapping(Routes.POSTS)
    @Operation(summary = "Create post", description = "Create a new post")
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody CreatePostRequest request, Authentication authentication) {
        Long userId = userService.getCurrentUserId(authentication.getName());
        PostResponse response = postService.createPost(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(Routes.POSTS)
    @Operation(summary = "Get all posts", description = "Get feed of all posts")
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping(Routes.POST_BY_ID)
    @Operation(summary = "Get post by ID", description = "Get specific post details")
    public ResponseEntity<PostDetailResponse> getPostById(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPostDetailById(postId));
    }

    @PutMapping(Routes.POST_BY_ID)
    @Operation(summary = "Update post", description = "Update authenticated user's post")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request,
            Authentication authentication) {
        Long userId = userService.getCurrentUserId(authentication.getName());
        return ResponseEntity.ok(postService.updatePost(postId, userId, request));
    }

    @DeleteMapping(Routes.POST_BY_ID)
    @Operation(summary = "Delete post", description = "Delete authenticated user's post")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId, Authentication authentication) {
        Long userId = userService.getCurrentUserId(authentication.getName());
        postService.deletePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(Routes.USER_POSTS)
    @Operation(summary = "Get user's posts", description = "Get all posts by specific user")
    public ResponseEntity<List<PostResponse>> getUserPosts(@PathVariable Long userId) {
        return ResponseEntity.ok(postService.getUserPosts(userId));
    }
}
