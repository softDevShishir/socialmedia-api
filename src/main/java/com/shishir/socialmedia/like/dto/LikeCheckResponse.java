package com.shishir.socialmedia.like.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeCheckResponse {

    private Boolean liked;
    private Long postId;
    private Long userId;
}
