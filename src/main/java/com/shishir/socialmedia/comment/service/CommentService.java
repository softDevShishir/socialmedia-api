package com.shishir.socialmedia.comment.service;

import com.shishir.socialmedia.comment.dto.CommentResponse;
import com.shishir.socialmedia.comment.dto.CreateCommentRequest;
import com.shishir.socialmedia.comment.dto.UpdateCommentRequest;
import com.shishir.socialmedia.comment.entity.Comment;
import com.shishir.socialmedia.comment.repository.CommentRepository;
import com.shishir.socialmedia.exception.BadRequestException;
import com.shishir.socialmedia.exception.ResourceNotFoundException;
import com.shishir.socialmedia.post.entity.Post;
import com.shishir.socialmedia.post.repository.PostRepository;
import com.shishir.socialmedia.user.entity.User;
import com.shishir.socialmedia.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentResponse createComment(Long postId, Long userId, CreateCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (post.getIsDeleted()) {
            throw new BadRequestException("Cannot comment on deleted post");
        }

        Comment comment = Comment.builder()
                .content(request.getContent())
                .post(post)
                .user(user)
                .likesCount(0)
                .isDeleted(false)
                .build();

        Comment savedComment = commentRepository.save(comment);
        postRepository.incrementCommentsCount(postId);

        log.info("Comment created successfully on post: {}", postId);
        return mapToCommentResponse(savedComment);
    }

    public CommentResponse getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        if (comment.getIsDeleted()) {
            throw new ResourceNotFoundException("Comment not found (deleted)");
        }

        return mapToCommentResponse(comment);
    }

    public List<CommentResponse> getPostComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (post.getIsDeleted()) {
            throw new ResourceNotFoundException("Post not found (deleted)");
        }

        return commentRepository.findByPostIdAndIsDeletedFalseOrderByCreatedAtDesc(postId)
                .stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());
    }

    public List<CommentResponse> getUserComments(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return commentRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());
    }

    public CommentResponse updateComment(Long commentId, Long userId, UpdateCommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You can only update your own comments");
        }

        if (comment.getIsDeleted()) {
            throw new BadRequestException("Cannot update deleted comment");
        }

        comment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);

        log.info("Comment updated successfully: {}", commentId);
        return mapToCommentResponse(updatedComment);
    }

    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You can only delete your own comments");
        }

        comment.setIsDeleted(true);
        commentRepository.save(comment);
        postRepository.decrementCommentsCount(comment.getPost().getId());

        log.info("Comment deleted successfully: {}", commentId);
    }

    public long getCommentCountByPost(Long postId) {
        return commentRepository.countByPostId(postId);
    }

    public long getCommentCountByUser(Long userId) {
        return commentRepository.countByUserId(userId);
    }

    private CommentResponse mapToCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .postId(comment.getPost().getId())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .userProfileImage(comment.getUser().getProfileImage())
                .likesCount(comment.getLikesCount())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
