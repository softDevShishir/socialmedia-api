package com.shishir.socialmedia.comment.controller;

import com.shishir.socialmedia.comment.dto.CommentResponse;
import com.shishir.socialmedia.comment.dto.CreateCommentRequest;
import com.shishir.socialmedia.comment.dto.UpdateCommentRequest;
import com.shishir.socialmedia.comment.service.CommentService;
import com.shishir.socialmedia.config.Routes;
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
@Tag(name = "Comments", description = "Comment management on posts")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    @PostMapping(Routes.POST_COMMENTS)
    @Operation(summary = "Create comment", description = "Create a new comment on a post")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {
        Long userId = userService.getCurrentUserId(authentication.getName());
        CommentResponse response = commentService.createComment(postId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(Routes.POST_COMMENTS)
    @Operation(summary = "Get post comments", description = "Get all comments on a post")
    public ResponseEntity<List<CommentResponse>> getPostComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getPostComments(postId));
    }

    @GetMapping(Routes.COMMENT_BY_ID)
    @Operation(summary = "Get comment", description = "Get specific comment details")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.getCommentById(commentId));
    }

    @PutMapping(Routes.COMMENT_BY_ID)
    @Operation(summary = "Update comment", description = "Update authenticated user's comment")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            Authentication authentication) {
        Long userId = userService.getCurrentUserId(authentication.getName());
        return ResponseEntity.ok(commentService.updateComment(commentId, userId, request));
    }

    @DeleteMapping(Routes.COMMENT_BY_ID)
    @Operation(summary = "Delete comment", description = "Delete authenticated user's comment")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, Authentication authentication) {
        Long userId = userService.getCurrentUserId(authentication.getName());
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
