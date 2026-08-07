package com.shishir.socialmedia.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Routes {

    // Base paths
    public static final String API = "/api";
    public static final String V1 = API + "/v1";

    // Auth routes
    public static final String AUTH = V1 + "/auth";
    public static final String AUTH_REGISTER = AUTH + "/register";
    public static final String AUTH_LOGIN = AUTH + "/login";
    public static final String AUTH_ME = AUTH + "/me";

    // User routes
    public static final String USERS = V1 + "/users";
    public static final String USER_BY_USERNAME = USERS + "/{username}";
    public static final String USER_BY_ID = USERS + "/{userId}";

    // Post routes
    public static final String POSTS = V1 + "/posts";
    public static final String POST_BY_ID = POSTS + "/{postId}";
    public static final String USER_POSTS = USERS + "/{userId}/posts";

    // Comment routes
    public static final String COMMENTS = V1 + "/comments";
    public static final String COMMENT_BY_ID = COMMENTS + "/{commentId}";
    public static final String POST_COMMENTS = POSTS + "/{postId}/comments";

    // Like routes
    public static final String LIKES = V1 + "/likes";
    public static final String POST_LIKES = POSTS + "/{postId}/likes";
    public static final String POST_LIKE_CHECK = POST_LIKES + "/check";

    // Follow routes
    public static final String FOLLOWS = V1 + "/follows";
    public static final String USER_FOLLOWERS = USERS + "/{userId}/followers";
    public static final String USER_FOLLOWING = USERS + "/{userId}/following";
    public static final String FOLLOW_USER = USERS + "/{userId}/follow";
    public static final String UNFOLLOW_USER = USERS + "/{userId}/unfollow";
    public static final String FOLLOW_CHECK = USERS + "/{userId}/follow/check";

    // Feed routes
    public static final String FEED = V1 + "/feed";
    public static final String TIMELINE = V1 + "/timeline";
    public static final String EXPLORE = V1 + "/explore";

    // Search routes
    public static final String SEARCH = V1 + "/search";
    public static final String SEARCH_USERS = SEARCH + "/users";
    public static final String SEARCH_POSTS = SEARCH + "/posts";
}
