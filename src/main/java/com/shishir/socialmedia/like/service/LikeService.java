package com.shishir.socialmedia.like.service;

import com.shishir.socialmedia.exception.BadRequestException;
import com.shishir.socialmedia.exception.DuplicateResourceException;
import com.shishir.socialmedia.exception.ResourceNotFoundException;
import com.shishir.socialmedia.like.dto.LikeCheckResponse;
import com.shishir.socialmedia.like.dto.LikeResponse;
import com.shishir.socialmedia.like.entity.Like;
import com.shishir.socialmedia.like.repository.LikeRepository;
import com.shishir.socialmedia.post.entity.Post;
import com.shishir.socialmedia.post.repository.PostRepository;
import com.shishir.socialmedia.user.entity.User;
import com.shishir.socialmedia.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public LikeResponse likePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (post.getIsDeleted()) {
            throw new BadRequestException("Cannot like deleted post");
        }

        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new DuplicateResourceException("User already liked this post");
        }

        Like like = Like.builder()
                .post(post)
                .user(user)
                .build();

        Like savedLike = likeRepository.save(like);
        postRepository.incrementLikesCount(postId);

        log.info("Post liked by user: {} on post: {}", userId, postId);
        return mapToLikeResponse(savedLike);
    }

    public void unlikePost(Long postId, Long userId) {
        Like like = likeRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Like not found"));

        likeRepository.delete(like);
        postRepository.decrementLikesCount(postId);

        log.info("Post unliked by user: {} on post: {}", userId, postId);
    }

    public List<LikeResponse> getPostLikes(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (post.getIsDeleted()) {
            throw new ResourceNotFoundException("Post not found (deleted)");
        }

        return likeRepository.findByPostIdOrderByCreatedAtDesc(postId)
                .stream()
                .map(this::mapToLikeResponse)
                .collect(Collectors.toList());
    }

    public List<LikeResponse> getUserLikes(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return likeRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToLikeResponse)
                .collect(Collectors.toList());
    }

    public LikeCheckResponse checkIfUserLikedPost(Long postId, Long userId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        boolean liked = likeRepository.existsByPostIdAndUserId(postId, userId);

        return LikeCheckResponse.builder()
                .liked(liked)
                .postId(postId)
                .userId(userId)
                .build();
    }

    public long getPostLikeCount(Long postId) {
        return likeRepository.countByPostId(postId);
    }

    public long getUserLikeCount(Long userId) {
        return likeRepository.countByUserId(userId);
    }

    private LikeResponse mapToLikeResponse(Like like) {
        return LikeResponse.builder()
                .id(like.getId())
                .postId(like.getPost().getId())
                .userId(like.getUser().getId())
                .username(like.getUser().getUsername())
                .userProfileImage(like.getUser().getProfileImage())
                .createdAt(like.getCreatedAt())
                .build();
    }
}
