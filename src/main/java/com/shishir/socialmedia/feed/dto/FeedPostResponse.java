package com.shishir.socialmedia.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedPostResponse {

    private Long id;
    private String content;
    private String mediaUrl;

    private Long userId;
    private String username;
    private String userProfileImage;
    private String userBio;

    private Integer likesCount;
    private Integer commentsCount;
    private Boolean likedByCurrentUser;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
