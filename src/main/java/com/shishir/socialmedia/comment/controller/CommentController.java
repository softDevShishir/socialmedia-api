package com.shishir.socialmedia.comment.controller;

import com.shishir.socialmedia.comment.dto.CommentResponse;
import com.shishir.socialmedia.comment.dto.CreateCommentRequest;
import com.shishir.socialmedia.comment.dto.UpdateCommentRequest;
import com.shishir.socialmedia.comment.service.CommentService;
import com.shishir.socialmedia.config.Routes;
import com.shishir.socialmedia.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(name = "Comments", description = "Comment management: create, read, update, delete")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    @PostMapping(Routes.POST_COMMENTS)
    @Operation(summary = "Create comment", description = "Create a new comment on a post")
    @ApiResponse(responseCode = "201", description = "Comment created")
    @ApiResponse(responseCode = "400", description = "Validation failed, or post is deleted")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Post not found")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<CommentResponse> createComment(
            @Parameter(description = "ID of the post to comment on") @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {
        Long userId = userService.getCurrentUserId(authentication.getName());
        CommentResponse response = commentService.createComment(postId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(Routes.POST_COMMENTS)
    @Operation(summary = "Get post comments", description = "Get all comments on a post")
    @ApiResponse(responseCode = "200", description = "List of comments")
    @ApiResponse(responseCode = "404", description = "Post not found")
    public ResponseEntity<List<CommentResponse>> getPostComments(
            @Parameter(description = "ID of the post whose comments to retrieve") @PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getPostComments(postId));
    }

    @GetMapping(Routes.COMMENT_BY_ID)
    @Operation(summary = "Get comment", description = "Get specific comment details")
    @ApiResponse(responseCode = "200", description = "Comment found")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    public ResponseEntity<CommentResponse> getCommentById(
            @Parameter(description = "ID of the comment to retrieve") @PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.getCommentById(commentId));
    }

    @PutMapping(Routes.COMMENT_BY_ID)
    @Operation(summary = "Update comment", description = "Update authenticated user's comment")
    @ApiResponse(responseCode = "200", description = "Comment updated")
    @ApiResponse(responseCode = "400", description = "Validation failed, or comment is deleted")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "403", description = "Cannot update another user's comment")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<CommentResponse> updateComment(
            @Parameter(description = "ID of the comment to update") @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            Authentication authentication) {
        Long userId = userService.getCurrentUserId(authentication.getName());
        return ResponseEntity.ok(commentService.updateComment(commentId, userId, request));
    }

    @DeleteMapping(Routes.COMMENT_BY_ID)
    @Operation(summary = "Delete comment", description = "Delete authenticated user's comment (soft delete)")
    @ApiResponse(responseCode = "204", description = "Comment deleted")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "403", description = "Cannot delete another user's comment")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "ID of the comment to delete") @PathVariable Long commentId, Authentication authentication) {
        Long userId = userService.getCurrentUserId(authentication.getName());
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
