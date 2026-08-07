package com.shishir.socialmedia.like;

import com.shishir.socialmedia.config.Routes;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class LikeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private User otherUser;
    private Post testPost;
    private String authToken;

    @BeforeEach
    void setUp() {
        likeRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .email("liketest@example.com")
                .password("hashed")
                .username("likeuser")
                .firstName("Like")
                .lastName("User")
                .build();
        testUser = userRepository.save(testUser);

        authToken = jwtTokenProvider.generateToken(testUser.getEmail(), testUser.getId().toString(), UserRole.USER.toString());

        otherUser = User.builder()
                .email("other@example.com")
                .password("hashed")
                .username("otheruser")
                .firstName("Other")
                .lastName("User")
                .build();
        otherUser = userRepository.save(otherUser);

        testPost = Post.builder()
                .content("Test post for likes")
                .user(testUser)
                .isDeleted(false)
                .build();
        testPost = postRepository.save(testPost);
    }

    @Test
    void testGetPostLikes() throws Exception {
        Like like1 = Like.builder()
                .post(testPost)
                .user(testUser)
                .build();
        Like like2 = Like.builder()
                .post(testPost)
                .user(otherUser)
                .build();
        likeRepository.save(like1);
        likeRepository.save(like2);

        mockMvc.perform(get(Routes.POST_LIKES.replace("{postId}", testPost.getId().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetPostLikesEmpty() throws Exception {
        mockMvc.perform(get(Routes.POST_LIKES.replace("{postId}", testPost.getId().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testCheckIfUserLikedPost() throws Exception {
        Like like = Like.builder()
                .post(testPost)
                .user(testUser)
                .build();
        likeRepository.save(like);

        mockMvc.perform(get(Routes.POST_LIKE_CHECK.replace("{postId}", testPost.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true))
                .andExpect(jsonPath("$.postId").value(testPost.getId()));
    }

    @Test
    void testCheckIfUserNotLikedPost() throws Exception {
        mockMvc.perform(get(Routes.POST_LIKE_CHECK.replace("{postId}", testPost.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(false));
    }

    @Test
    void testCheckIfUserLikedPostUnauthenticatedRejected() throws Exception {
        mockMvc.perform(get(Routes.POST_LIKE_CHECK.replace("{postId}", testPost.getId().toString())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUniqueLikeConstraint() {
        Like like1 = Like.builder()
                .post(testPost)
                .user(testUser)
                .build();
        likeRepository.save(like1);

        Like duplicateLike = Like.builder()
                .post(testPost)
                .user(testUser)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> likeRepository.save(duplicateLike));
    }

    @Test
    void testMultipleUsersCanLikeSamePost() {
        Like like1 = Like.builder()
                .post(testPost)
                .user(testUser)
                .build();
        Like like2 = Like.builder()
                .post(testPost)
                .user(otherUser)
                .build();

        likeRepository.save(like1);
        likeRepository.save(like2);

        assertEquals(2, likeRepository.countByPostId(testPost.getId()));
    }

    @Test
    void testLikePostIncrementsLikesCount() throws Exception {
        mockMvc.perform(post(Routes.POST_LIKES.replace("{postId}", testPost.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.postId").value(testPost.getId()))
                .andExpect(jsonPath("$.userId").value(testUser.getId()));

        Post updatedPost = postRepository.findById(testPost.getId()).orElseThrow();
        assertEquals(1, updatedPost.getLikesCount());
    }

    @Test
    void testLikePostUnauthenticatedRejected() throws Exception {
        mockMvc.perform(post(Routes.POST_LIKES.replace("{postId}", testPost.getId().toString())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLikePostAlreadyLikedConflict() throws Exception {
        mockMvc.perform(post(Routes.POST_LIKES.replace("{postId}", testPost.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken))
                .andExpect(status().isCreated());

        mockMvc.perform(post(Routes.POST_LIKES.replace("{postId}", testPost.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken))
                .andExpect(status().isConflict());
    }

    @Test
    void testUnlikePostDecrementsLikesCount() throws Exception {
        mockMvc.perform(post(Routes.POST_LIKES.replace("{postId}", testPost.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken))
                .andExpect(status().isCreated());
        assertEquals(1, postRepository.findById(testPost.getId()).orElseThrow().getLikesCount());

        mockMvc.perform(delete(Routes.POST_LIKES.replace("{postId}", testPost.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken))
                .andExpect(status().isNoContent());

        assertEquals(0, postRepository.findById(testPost.getId()).orElseThrow().getLikesCount());
    }

    @Test
    void testUnlikePostNotLikedNotFound() throws Exception {
        mockMvc.perform(delete(Routes.POST_LIKES.replace("{postId}", testPost.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }
}
