package com.shishir.socialmedia.search.controller;

import com.shishir.socialmedia.config.Routes;
import com.shishir.socialmedia.exception.BadRequestException;
import com.shishir.socialmedia.search.dto.PaginatedSearchResponse;
import com.shishir.socialmedia.search.dto.SearchPostResponse;
import com.shishir.socialmedia.search.dto.SearchUserResponse;
import com.shishir.socialmedia.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Search", description = "Search: find users and posts")
public class SearchController {

    private static final int MAX_PAGE_SIZE = 50;

    private final SearchService searchService;

    @GetMapping(Routes.SEARCH_USERS)
    @Operation(summary = "Search users", description = "Search for users by username (case-insensitive, paginated)")
    @ApiResponse(responseCode = "200", description = "Search results")
    @ApiResponse(responseCode = "400", description = "Invalid search query or pagination parameters")
    public ResponseEntity<PaginatedSearchResponse<SearchUserResponse>> searchUsers(
            @Parameter(description = "Username substring to search for (case-insensitive)") @RequestParam String query,
            @Parameter(description = "Zero-based page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of results per page (max " + MAX_PAGE_SIZE + ")") @RequestParam(defaultValue = "10") int pageSize) {
        validatePagination(page, pageSize);
        return ResponseEntity.ok(searchService.searchUsers(query, page, pageSize));
    }

    @GetMapping(Routes.SEARCH_POSTS)
    @Operation(summary = "Search posts", description = "Search for posts by content (case-insensitive, paginated)")
    @ApiResponse(responseCode = "200", description = "Search results")
    @ApiResponse(responseCode = "400", description = "Invalid search query or pagination parameters")
    public ResponseEntity<PaginatedSearchResponse<SearchPostResponse>> searchPosts(
            @Parameter(description = "Content substring to search for (case-insensitive)") @RequestParam String query,
            @Parameter(description = "Zero-based page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of results per page (max " + MAX_PAGE_SIZE + ")") @RequestParam(defaultValue = "10") int pageSize) {
        validatePagination(page, pageSize);
        return ResponseEntity.ok(searchService.searchPosts(query, page, pageSize));
    }

    private void validatePagination(int page, int pageSize) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be negative");
        }
        if (pageSize <= 0 || pageSize > MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }
    }
}
