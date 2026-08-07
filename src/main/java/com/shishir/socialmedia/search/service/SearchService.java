package com.shishir.socialmedia.search.service;

import com.shishir.socialmedia.exception.BadRequestException;
import com.shishir.socialmedia.post.entity.Post;
import com.shishir.socialmedia.post.repository.PostRepository;
import com.shishir.socialmedia.search.dto.PaginatedSearchResponse;
import com.shishir.socialmedia.search.dto.SearchPostResponse;
import com.shishir.socialmedia.search.dto.SearchUserResponse;
import com.shishir.socialmedia.user.entity.User;
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
public class SearchService {

    private static final int MAX_QUERY_LENGTH = 100;

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public PaginatedSearchResponse<SearchUserResponse> searchUsers(String query, int page, int pageSize) {
        String trimmedQuery = validateQuery(query);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        Page<User> usersPage = userRepository.findByUsernameContainingIgnoreCaseAndIsActiveTrue(trimmedQuery, pageable);

        List<SearchUserResponse> users = usersPage.getContent()
                .stream()
                .map(this::mapToSearchUserResponse)
                .collect(Collectors.toList());

        log.info("User search completed. Query: {}, Results: {}", trimmedQuery, usersPage.getTotalElements());

        return PaginatedSearchResponse.<SearchUserResponse>builder()
                .results(users)
                .query(trimmedQuery)
                .type("users")
                .currentPage(page)
                .pageSize(pageSize)
                .totalElements(usersPage.getTotalElements())
                .totalPages(usersPage.getTotalPages())
                .hasNext(usersPage.hasNext())
                .hasPrevious(usersPage.hasPrevious())
                .build();
    }

    public PaginatedSearchResponse<SearchPostResponse> searchPosts(String query, int page, int pageSize) {
        String trimmedQuery = validateQuery(query);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        Page<Post> postsPage = postRepository.findByContentContainingIgnoreCaseAndIsDeletedFalse(trimmedQuery, pageable);

        List<SearchPostResponse> posts = postsPage.getContent()
                .stream()
                .map(this::mapToSearchPostResponse)
                .collect(Collectors.toList());

        log.info("Post search completed. Query: {}, Results: {}", trimmedQuery, postsPage.getTotalElements());

        return PaginatedSearchResponse.<SearchPostResponse>builder()
                .results(posts)
                .query(trimmedQuery)
                .type("posts")
                .currentPage(page)
                .pageSize(pageSize)
                .totalElements(postsPage.getTotalElements())
                .totalPages(postsPage.getTotalPages())
                .hasNext(postsPage.hasNext())
                .hasPrevious(postsPage.hasPrevious())
                .build();
    }

    public PaginatedSearchResponse<SearchPostResponse> searchPostsByUsername(String username, int page, int pageSize) {
        if (username == null || username.trim().isEmpty()) {
            throw new BadRequestException("Username cannot be empty");
        }

        String trimmedUsername = username.trim();

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        Page<Post> postsPage = postRepository.findByUserUsernameContainingIgnoreCaseAndIsDeletedFalse(trimmedUsername, pageable);

        List<SearchPostResponse> posts = postsPage.getContent()
                .stream()
                .map(this::mapToSearchPostResponse)
                .collect(Collectors.toList());

        log.info("Post search by username completed. Username: {}, Results: {}", trimmedUsername, postsPage.getTotalElements());

        return PaginatedSearchResponse.<SearchPostResponse>builder()
                .results(posts)
                .query(trimmedUsername)
                .type("posts")
                .currentPage(page)
                .pageSize(pageSize)
                .totalElements(postsPage.getTotalElements())
                .totalPages(postsPage.getTotalPages())
                .hasNext(postsPage.hasNext())
                .hasPrevious(postsPage.hasPrevious())
                .build();
    }

    private String validateQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new BadRequestException("Search query cannot be empty");
        }
        if (query.length() > MAX_QUERY_LENGTH) {
            throw new BadRequestException("Search query cannot exceed " + MAX_QUERY_LENGTH + " characters");
        }
        return query.trim();
    }

    private SearchUserResponse mapToSearchUserResponse(User user) {
        return SearchUserResponse.builder()
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

    private SearchPostResponse mapToSearchPostResponse(Post post) {
        return SearchPostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .mediaUrl(post.getMediaUrl())
                .userId(post.getUser().getId())
                .username(post.getUser().getUsername())
                .userProfileImage(post.getUser().getProfileImage())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
