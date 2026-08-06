package com.shishir.socialmedia.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDetailResponse {

    private Long id;
    private String content;
    private String mediaUrl;
    private Long userId;
    private String username;
    private String userEmail;
    private String userProfileImage;
    private String userBio;
    private Integer likesCount;
    private Integer commentsCount;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
