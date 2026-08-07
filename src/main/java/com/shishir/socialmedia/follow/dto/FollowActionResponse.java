package com.shishir.socialmedia.follow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowActionResponse {

    private Long id;
    private Long followerId;
    private String followerUsername;
    private Long followingId;
    private String followingUsername;
    private LocalDateTime followedAt;
    private String action;
}
