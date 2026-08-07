package com.shishir.socialmedia.feed.service;

import com.shishir.socialmedia.exception.ResourceNotFoundException;
import com.shishir.socialmedia.feed.dto.FeedPostResponse;
import com.shishir.socialmedia.feed.dto.PaginatedFeedResponse;
import com.shishir.socialmedia.follow.repository.FollowRepository;
import com.shishir.socialmedia.like.repository.LikeRepository;
import com.shishir.socialmedia.post.entity.Post;
import com.shishir.socialmedia.post.repository.PostRepository;
import com.shishir.socialmedia.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class FeedService {

    private final PostRepository postRepository;
    private final FollowRepository followRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    public PaginatedFeedResponse getUserFeed(Long userId, int page, int pageSize) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        List<Long> followingIds = followRepository.findByFollowerIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(follow -> follow.getFollowing().getId())
                .collect(Collectors.toList());
        followingIds.add(userId);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        Page<Post> postsPage = postRepository.findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc(followingIds, pageable);

        List<FeedPostResponse> feedPosts = postsPage.getContent()
                .stream()
                .map(post -> mapToFeedPostResponse(post, userId))
                .collect(Collectors.toList());

        log.info("Feed retrieved for user: {}", userId);

        return buildPaginatedResponse(feedPosts, postsPage, page, pageSize);
    }

    public PaginatedFeedResponse getTimeline(Long userId, Long currentUserId, int page, int pageSize) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        Page<Post> postsPage = postRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId, pageable);

        List<FeedPostResponse> feedPosts = postsPage.getContent()
                .stream()
                .map(post -> mapToFeedPostResponse(post, currentUserId))
                .collect(Collectors.toList());

        log.info("Timeline retrieved for user: {}", userId);

        return buildPaginatedResponse(feedPosts, postsPage, page, pageSize);
    }

    public PaginatedFeedResponse getExploreFeed(Long currentUserId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        Page<Post> postsPage = postRepository.findByIsDeletedFalseOrderByCreatedAtDesc(pageable);

        List<FeedPostResponse> feedPosts = postsPage.getContent()
                .stream()
                .map(post -> mapToFeedPostResponse(post, currentUserId))
                .collect(Collectors.toList());

        log.info("Explore feed retrieved");

        return buildPaginatedResponse(feedPosts, postsPage, page, pageSize);
    }

    private PaginatedFeedResponse buildPaginatedResponse(List<FeedPostResponse> feedPosts, Page<Post> postsPage, int page, int pageSize) {
        return PaginatedFeedResponse.builder()
                .posts(feedPosts)
                .currentPage(page)
                .pageSize(pageSize)
                .totalElements(postsPage.getTotalElements())
                .totalPages(postsPage.getTotalPages())
                .hasNext(postsPage.hasNext())
                .hasPrevious(postsPage.hasPrevious())
                .build();
    }

    private FeedPostResponse mapToFeedPostResponse(Post post, Long currentUserId) {
        boolean likedByCurrentUser = currentUserId != null
                && likeRepository.existsByPostIdAndUserId(post.getId(), currentUserId);

        return FeedPostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .mediaUrl(post.getMediaUrl())
                .userId(post.getUser().getId())
                .username(post.getUser().getUsername())
                .userProfileImage(post.getUser().getProfileImage())
                .userBio(post.getUser().getBio())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .likedByCurrentUser(likedByCurrentUser)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
