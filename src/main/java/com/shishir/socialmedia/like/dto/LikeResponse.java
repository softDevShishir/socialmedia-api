package com.shishir.socialmedia.like.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeResponse {

    private Long id;
    private Long postId;
    private Long userId;
    private String username;
    private String userProfileImage;
    private LocalDateTime createdAt;
}
