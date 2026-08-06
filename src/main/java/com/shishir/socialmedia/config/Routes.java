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
}
