package com.shishir.socialmedia.post.service;

import com.shishir.socialmedia.exception.BadRequestException;
import com.shishir.socialmedia.exception.ResourceNotFoundException;
import com.shishir.socialmedia.post.dto.CreatePostRequest;
import com.shishir.socialmedia.post.dto.PostDetailResponse;
import com.shishir.socialmedia.post.dto.PostResponse;
import com.shishir.socialmedia.post.dto.UpdatePostRequest;
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
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostResponse createPost(Long userId, CreatePostRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Post post = Post.builder()
                .content(request.getContent())
                .mediaUrl(request.getMediaUrl())
                .user(user)
                .likesCount(0)
                .commentsCount(0)
                .isDeleted(false)
                .build();

        Post savedPost = postRepository.save(post);
        log.info("Post created successfully by user: {}", user.getUsername());
        return mapToPostResponse(savedPost);
    }

    public PostResponse getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (post.getIsDeleted()) {
            throw new ResourceNotFoundException("Post not found (deleted)");
        }

        return mapToPostResponse(post);
    }

    public PostDetailResponse getPostDetailById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (post.getIsDeleted()) {
            throw new ResourceNotFoundException("Post not found (deleted)");
        }

        return mapToPostDetailResponse(post);
    }

    public List<PostResponse> getAllPosts() {
        return postRepository.findByIsDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToPostResponse)
                .collect(Collectors.toList());
    }

    public List<PostResponse> getUserPosts(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return postRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToPostResponse)
                .collect(Collectors.toList());
    }

    public PostResponse updatePost(Long postId, Long userId, UpdatePostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (!post.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You can only update your own posts");
        }

        if (post.getIsDeleted()) {
            throw new BadRequestException("Cannot update deleted post");
        }

        post.setContent(request.getContent());
        post.setMediaUrl(request.getMediaUrl());

        Post updatedPost = postRepository.save(post);
        log.info("Post updated successfully: {}", postId);
        return mapToPostResponse(updatedPost);
    }

    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (!post.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You can only delete your own posts");
        }

        post.setIsDeleted(true);
        postRepository.save(post);
        log.info("Post deleted successfully: {}", postId);
    }

    public long getPostCountByUser(Long userId) {
        return postRepository.countByUserId(userId);
    }

    private PostResponse mapToPostResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .mediaUrl(post.getMediaUrl())
                .userId(post.getUser().getId())
                .username(post.getUser().getUsername())
                .userProfileImage(post.getUser().getProfileImage())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    private PostDetailResponse mapToPostDetailResponse(Post post) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .mediaUrl(post.getMediaUrl())
                .userId(post.getUser().getId())
                .username(post.getUser().getUsername())
                .userProfileImage(post.getUser().getProfileImage())
                .userBio(post.getUser().getBio())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .isDeleted(post.getIsDeleted())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
