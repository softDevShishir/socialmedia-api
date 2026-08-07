package com.shishir.socialmedia.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
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
                                .description("Enter JWT token")));
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("Authentication")
                .pathsToMatch("/api/v1/auth/**")
                .displayName("Authentication - register, login, get current user")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("Users")
                .pathsToMatch("/api/v1/users/**")
                .displayName("Users - view profiles, update profile, follow/unfollow")
                .build();
    }

    @Bean
    public GroupedOpenApi postApi() {
        return GroupedOpenApi.builder()
                .group("Posts")
                .pathsToMatch("/api/v1/posts/**", "/api/v1/users/*/posts")
                .displayName("Posts - create, read, update, delete posts")
                .build();
    }

    @Bean
    public GroupedOpenApi commentApi() {
        return GroupedOpenApi.builder()
                .group("Comments")
                .pathsToMatch("/api/v1/comments/**", "/api/v1/posts/*/comments")
                .displayName("Comments - create, read, update, delete comments")
                .build();
    }

    @Bean
    public GroupedOpenApi likeApi() {
        return GroupedOpenApi.builder()
                .group("Likes")
                .pathsToMatch("/api/v1/posts/*/likes", "/api/v1/posts/*/likes/check")
                .displayName("Likes - like, unlike posts, check likes")
                .build();
    }

    @Bean
    public GroupedOpenApi followApi() {
        return GroupedOpenApi.builder()
                .group("Follow")
                .pathsToMatch("/api/v1/users/*/followers", "/api/v1/users/*/following",
                        "/api/v1/users/*/follow", "/api/v1/users/*/unfollow", "/api/v1/users/*/follow/check")
                .displayName("Follow - follow, unfollow, view followers/following")
                .build();
    }

    @Bean
    public GroupedOpenApi feedApi() {
        return GroupedOpenApi.builder()
                .group("Feed")
                .pathsToMatch("/api/v1/feed/**", "/api/v1/timeline/**", "/api/v1/explore/**")
                .displayName("Feed - personalized feed, user timeline, explore page")
                .build();
    }

    @Bean
    public GroupedOpenApi searchApi() {
        return GroupedOpenApi.builder()
                .group("Search")
                .pathsToMatch("/api/v1/search/**")
                .displayName("Search - find users and posts")
                .build();
    }
}
