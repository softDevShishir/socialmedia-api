package com.shishir.socialmedia.follow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowCheckResponse {

    private Boolean following;
    private Long userId;
    private String username;
}
