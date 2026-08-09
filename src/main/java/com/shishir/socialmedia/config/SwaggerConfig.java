package com.shishir.socialmedia.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Social Media REST API")
                        .version("1.0.0")
                        .description("Production-ready Social Media API with Spring Boot. Features: user authentication, posts, comments, likes, follows, feed, timeline, and search.")
                        .contact(new Contact()
                                .name("Shishir")
                                .email("softdevshishir@gmail.com")
                                .url("https://github.com/softDevShishir"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development"),
                        new Server()
                                .url("https://socialmedia-api-render.onrender.com")
                                .description("Production (Render)")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token")))
                .tags(List.of(
                        new Tag().name("Authentication").description("User authentication: register, login, get current user"),
                        new Tag().name("Users").description("User profiles: view, update, follow/unfollow"),
                        new Tag().name("Posts").description("Post management: create, read, update, delete"),
                        new Tag().name("Comments").description("Comment management: create, read, update, delete"),
                        new Tag().name("Likes").description("Like management on posts: like, unlike, check likes"),
                        new Tag().name("Follow").description("User follow/unfollow management"),
                        new Tag().name("Feed").description("Feed and timeline: personalized feed, user timeline, explore"),
                        new Tag().name("Search").description("Search: find users and posts")));
    }

    /**
     * Spring's request-mapping registration order (which drives the raw order springdoc
     * discovers paths in) does not follow controller source order, so it's reordered
     * explicitly here to match the documented manual test flow: Auth -> Users -> Posts ->
     * Comments -> Likes -> Follow -> Feed -> Search. Operations sharing one path template
     * (e.g. GET/PUT/DELETE on POST_BY_ID) still render in OpenAPI's fixed get/put/post/delete
     * field order regardless of this list.
     */
    @Bean
    public GlobalOpenApiCustomizer pathOrderCustomizer() {
        List<String> order = List.of(
                Routes.AUTH_REGISTER,
                Routes.AUTH_LOGIN,
                Routes.AUTH_ME,
                Routes.USERS,
                Routes.USER_BY_USERNAME,
                Routes.USER_BY_ID,
                Routes.POSTS,
                Routes.POST_BY_ID,
                Routes.USER_POSTS,
                Routes.POST_COMMENTS,
                Routes.COMMENT_BY_ID,
                Routes.POST_LIKES,
                Routes.POST_LIKE_CHECK,
                Routes.FOLLOW_USER,
                Routes.FOLLOW_CHECK,
                Routes.USER_FOLLOWERS,
                Routes.USER_FOLLOWING,
                Routes.UNFOLLOW_USER,
                Routes.FEED,
                Routes.TIMELINE,
                Routes.EXPLORE,
                Routes.SEARCH_USERS,
                Routes.SEARCH_POSTS);

        return openApi -> {
            Paths original = openApi.getPaths();
            if (original == null) {
                return;
            }
            Paths ordered = new Paths();
            for (String path : order) {
                var pathItem = original.remove(path);
                if (pathItem != null) {
                    ordered.addPathItem(path, pathItem);
                }
            }
            original.forEach(ordered::addPathItem);
            openApi.setPaths(ordered);
        };
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("1-Authentication")
                .pathsToMatch("/api/v1/auth/**")
                .displayName("1. Authentication - register, login, get current user")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("2-Users")
                .pathsToMatch("/api/v1/users/**")
                .displayName("2. Users - view profiles, update profile, follow/unfollow")
                .build();
    }

    @Bean
    public GroupedOpenApi postApi() {
        return GroupedOpenApi.builder()
                .group("3-Posts")
                .pathsToMatch("/api/v1/posts/**", "/api/v1/users/*/posts")
                .displayName("3. Posts - create, read, update, delete posts")
                .build();
    }

    @Bean
    public GroupedOpenApi commentApi() {
        return GroupedOpenApi.builder()
                .group("4-Comments")
                .pathsToMatch("/api/v1/comments/**", "/api/v1/posts/*/comments")
                .displayName("4. Comments - create, read, update, delete comments")
                .build();
    }

    @Bean
    public GroupedOpenApi likeApi() {
        return GroupedOpenApi.builder()
                .group("5-Likes")
                .pathsToMatch("/api/v1/posts/*/likes", "/api/v1/posts/*/likes/check")
                .displayName("5. Likes - like, unlike posts, check likes")
                .build();
    }

    @Bean
    public GroupedOpenApi followApi() {
        return GroupedOpenApi.builder()
                .group("6-Follow")
                .pathsToMatch("/api/v1/users/*/followers", "/api/v1/users/*/following",
                        "/api/v1/users/*/follow", "/api/v1/users/*/unfollow", "/api/v1/users/*/follow/check")
                .displayName("6. Follow - follow, unfollow, view followers/following")
                .build();
    }

    @Bean
    public GroupedOpenApi feedApi() {
        return GroupedOpenApi.builder()
                .group("7-Feed")
                .pathsToMatch("/api/v1/feed/**", "/api/v1/timeline/**", "/api/v1/explore/**")
                .displayName("7. Feed - personalized feed, user timeline, explore page")
                .build();
    }

    @Bean
    public GroupedOpenApi searchApi() {
        return GroupedOpenApi.builder()
                .group("8-Search")
                .pathsToMatch("/api/v1/search/**")
                .displayName("8. Search - find users and posts")
                .build();
    }
}
