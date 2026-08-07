package com.shishir.socialmedia.feed;

import com.shishir.socialmedia.config.Routes;
import com.shishir.socialmedia.follow.entity.Follow;
import com.shishir.socialmedia.follow.repository.FollowRepository;
import com.shishir.socialmedia.like.entity.Like;
import com.shishir.socialmedia.like.repository.LikeRepository;
import com.shishir.socialmedia.post.entity.Post;
import com.shishir.socialmedia.post.repository.PostRepository;
import com.shishir.socialmedia.security.JwtTokenProvider;
import com.shishir.socialmedia.user.entity.User;
import com.shishir.socialmedia.user.entity.UserRole;
import com.shishir.socialmedia.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class FeedControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User user1;
    private User user2;
    private User user3;
    private String user1Token;
    private String user2Token;

    @BeforeEach
    void setUp() {
        likeRepository.deleteAll();
        postRepository.deleteAll();
        followRepository.deleteAll();
        userRepository.deleteAll();

        user1 = User.builder()
                .email("user1@example.com")
                .password("hashed")
                .username("user1")
                .firstName("User")
                .lastName("One")
                .build();
        user1 = userRepository.save(user1);
        user1Token = jwtTokenProvider.generateToken(user1.getEmail(), user1.getId().toString(), UserRole.USER.toString());

        user2 = User.builder()
                .email("user2@example.com")
                .password("hashed")
                .username("user2")
                .firstName("User")
                .lastName("Two")
                .build();
        user2 = userRepository.save(user2);
        user2Token = jwtTokenProvider.generateToken(user2.getEmail(), user2.getId().toString(), UserRole.USER.toString());

        user3 = User.builder()
                .email("user3@example.com")
                .password("hashed")
                .username("user3")
                .firstName("User")
                .lastName("Three")
                .build();
        user3 = userRepository.save(user3);
    }

    @Test
    void testGetExploreFeed() throws Exception {
        Post post1 = Post.builder()
                .content("Post 1")
                .user(user1)
                .isDeleted(false)
                .build();
        Post post2 = Post.builder()
                .content("Post 2")
                .user(user2)
                .isDeleted(false)
                .build();
        Post post3 = Post.builder()
                .content("Post 3")
                .user(user3)
                .isDeleted(false)
                .build();
        postRepository.save(post1);
        postRepository.save(post2);
        postRepository.save(post3);

        mockMvc.perform(get(Routes.EXPLORE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void testGetTimelineByUserId() throws Exception {
        Post post1 = Post.builder()
                .content("User1 Post 1")
                .user(user1)
                .isDeleted(false)
                .build();
        Post post2 = Post.builder()
                .content("User1 Post 2")
                .user(user1)
                .isDeleted(false)
                .build();
        Post post3 = Post.builder()
                .content("User2 Post")
                .user(user2)
                .isDeleted(false)
                .build();
        postRepository.save(post1);
        postRepository.save(post2);
        postRepository.save(post3);

        mockMvc.perform(get(Routes.TIMELINE + "?userId=" + user1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void testExplodeFeedPagination() throws Exception {
        for (int i = 0; i < 15; i++) {
            Post post = Post.builder()
                    .content("Post " + i)
                    .user(i % 2 == 0 ? user1 : user2)
                    .isDeleted(false)
                    .build();
            postRepository.save(post);
        }

        mockMvc.perform(get(Routes.EXPLORE + "?page=0&pageSize=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(false));

        mockMvc.perform(get(Routes.EXPLORE + "?page=1&pageSize=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.length()").value(5))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(true));
    }

    @Test
    void testDeletedPostsNotInFeed() throws Exception {
        Post post1 = Post.builder()
                .content("Active post")
                .user(user1)
                .isDeleted(false)
                .build();
        Post post2 = Post.builder()
                .content("Deleted post")
                .user(user2)
                .isDeleted(true)
                .build();
        postRepository.save(post1);
        postRepository.save(post2);

        mockMvc.perform(get(Routes.EXPLORE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testInvalidPageParameters() throws Exception {
        mockMvc.perform(get(Routes.EXPLORE + "?page=-1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get(Routes.EXPLORE + "?pageSize=0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get(Routes.EXPLORE + "?pageSize=101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUserFeedIncludesOwnAndFollowedPostsOnly() throws Exception {
        Post ownPost = postRepository.save(Post.builder()
                .content("My own post")
                .user(user1)
                .isDeleted(false)
                .build());
        Post followedPost = postRepository.save(Post.builder()
                .content("Followed user's post")
                .user(user2)
                .isDeleted(false)
                .build());
        postRepository.save(Post.builder()
                .content("Unrelated user's post")
                .user(user3)
                .isDeleted(false)
                .build());

        followRepository.save(Follow.builder().follower(user1).following(user2).build());

        mockMvc.perform(get(Routes.FEED)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void testUserFeedUnauthenticatedRejected() throws Exception {
        mockMvc.perform(get(Routes.FEED))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLikedByCurrentUserReflectsViewerNotPostAuthor() throws Exception {
        Post post = postRepository.save(Post.builder()
                .content("A post user1 wrote")
                .user(user1)
                .isDeleted(false)
                .build());

        likeRepository.save(Like.builder().post(post).user(user2).build());

        mockMvc.perform(get(Routes.TIMELINE + "?userId=" + user1.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].likedByCurrentUser").value(true));

        mockMvc.perform(get(Routes.TIMELINE + "?userId=" + user1.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].likedByCurrentUser").value(false));

        mockMvc.perform(get(Routes.TIMELINE + "?userId=" + user1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].likedByCurrentUser").value(false));
    }
}
