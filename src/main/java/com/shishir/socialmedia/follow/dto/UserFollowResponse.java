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
public class UserFollowResponse {

    private Long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String bio;
    private String profileImage;
    private Integer followerCount;
    private Integer followingCount;
    private LocalDateTime createdAt;
}
