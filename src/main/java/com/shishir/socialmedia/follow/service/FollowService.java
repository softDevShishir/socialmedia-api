package com.shishir.socialmedia.follow.service;

import com.shishir.socialmedia.exception.BadRequestException;
import com.shishir.socialmedia.exception.DuplicateResourceException;
import com.shishir.socialmedia.exception.ResourceNotFoundException;
import com.shishir.socialmedia.follow.dto.FollowActionResponse;
import com.shishir.socialmedia.follow.dto.FollowCheckResponse;
import com.shishir.socialmedia.follow.dto.UserFollowResponse;
import com.shishir.socialmedia.follow.entity.Follow;
import com.shishir.socialmedia.follow.repository.FollowRepository;
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
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowActionResponse followUser(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new BadRequestException("User cannot follow themselves");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException("Follower user not found with id: " + followerId));

        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new ResourceNotFoundException("Following user not found with id: " + followingId));

        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new DuplicateResourceException("User already following this user");
        }

        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();

        Follow savedFollow = followRepository.save(follow);

        userRepository.incrementFollowerCount(followingId);
        userRepository.incrementFollowingCount(followerId);

        log.info("User {} followed user {}", followerId, followingId);

        return FollowActionResponse.builder()
                .id(savedFollow.getId())
                .followerId(follower.getId())
                .followerUsername(follower.getUsername())
                .followingId(following.getId())
                .followingUsername(following.getUsername())
                .followedAt(savedFollow.getCreatedAt())
                .action("FOLLOWED")
                .build();
    }

    public FollowActionResponse unfollowUser(Long followerId, Long followingId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new ResourceNotFoundException("Follow relationship not found"));

        User follower = follow.getFollower();
        User following = follow.getFollowing();

        followRepository.delete(follow);

        userRepository.decrementFollowerCount(followingId);
        userRepository.decrementFollowingCount(followerId);

        log.info("User {} unfollowed user {}", followerId, followingId);

        return FollowActionResponse.builder()
                .id(follow.getId())
                .followerId(follower.getId())
                .followerUsername(follower.getUsername())
                .followingId(following.getId())
                .followingUsername(following.getUsername())
                .followedAt(follow.getCreatedAt())
                .action("UNFOLLOWED")
                .build();
    }

    public List<UserFollowResponse> getUserFollowers(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return followRepository.findByFollowingIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(follow -> mapToUserFollowResponse(follow.getFollower()))
                .collect(Collectors.toList());
    }

    public List<UserFollowResponse> getUserFollowing(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return followRepository.findByFollowerIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(follow -> mapToUserFollowResponse(follow.getFollowing()))
                .collect(Collectors.toList());
    }

    public FollowCheckResponse checkIfFollowing(Long followerId, Long followingId) {
        User user = userRepository.findById(followingId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + followingId));

        boolean following = followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);

        return FollowCheckResponse.builder()
                .following(following)
                .userId(followingId)
                .username(user.getUsername())
                .build();
    }

    public long getUserFollowerCount(Long userId) {
        return followRepository.countByFollowingId(userId);
    }

    public long getUserFollowingCount(Long userId) {
        return followRepository.countByFollowerId(userId);
    }

    private UserFollowResponse mapToUserFollowResponse(User user) {
        return UserFollowResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .bio(user.getBio())
                .profileImage(user.getProfileImage())
                .followerCount(user.getFollowerCount())
                .followingCount(user.getFollowingCount())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
