package com.shishir.socialmedia.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchPostResponse {

    private Long id;
    private String content;
    private String mediaUrl;
    private Long userId;
    private String username;
    private String userProfileImage;
    private Integer likesCount;
    private Integer commentsCount;
    private LocalDateTime createdAt;
}
