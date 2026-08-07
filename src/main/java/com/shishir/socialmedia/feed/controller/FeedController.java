package com.shishir.socialmedia.feed.controller;

import com.shishir.socialmedia.config.Routes;
import com.shishir.socialmedia.exception.BadRequestException;
import com.shishir.socialmedia.feed.dto.PaginatedFeedResponse;
import com.shishir.socialmedia.feed.service.FeedService;
import com.shishir.socialmedia.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Feed", description = "User feed and timeline management")
public class FeedController {

    private final FeedService feedService;
    private final UserService userService;

    @GetMapping(Routes.FEED)
    @Operation(summary = "Get user feed", description = "Get personalized feed with posts from followed users")
    public ResponseEntity<PaginatedFeedResponse> getUserFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            Authentication authentication) {
        validatePagination(page, pageSize);
        Long userId = userService.getCurrentUserId(authentication.getName());
        return ResponseEntity.ok(feedService.getUserFeed(userId, page, pageSize));
    }

    @GetMapping(Routes.TIMELINE)
    @Operation(summary = "Get user timeline", description = "Get all posts by a specific user")
    public ResponseEntity<PaginatedFeedResponse> getTimeline(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            Authentication authentication) {
        validatePagination(page, pageSize);
        Long currentUserId = resolveOptionalCurrentUserId(authentication);
        return ResponseEntity.ok(feedService.getTimeline(userId, currentUserId, page, pageSize));
    }

    @GetMapping(Routes.EXPLORE)
    @Operation(summary = "Get explore feed", description = "Get feed of all public posts (explore page)")
    public ResponseEntity<PaginatedFeedResponse> getExploreFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            Authentication authentication) {
        validatePagination(page, pageSize);
        Long currentUserId = resolveOptionalCurrentUserId(authentication);
        return ResponseEntity.ok(feedService.getExploreFeed(currentUserId, page, pageSize));
    }

    private void validatePagination(int page, int pageSize) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be negative");
        }
        if (pageSize <= 0 || pageSize > 100) {
            throw new BadRequestException("Page size must be between 1 and 100");
        }
    }

    private Long resolveOptionalCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return userService.getCurrentUserId(authentication.getName());
    }
}
