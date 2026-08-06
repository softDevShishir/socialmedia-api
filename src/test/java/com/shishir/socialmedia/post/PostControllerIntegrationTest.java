package com.shishir.socialmedia.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shishir.socialmedia.config.Routes;
import com.shishir.socialmedia.post.dto.CreatePostRequest;
import com.shishir.socialmedia.post.dto.UpdatePostRequest;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class PostControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .email("posttest@example.com")
                .password("hashed")
                .username("postuser")
                .firstName("Post")
                .lastName("User")
                .build();
        testUser = userRepository.save(testUser);

        authToken = jwtTokenProvider.generateToken(testUser.getEmail(), testUser.getId().toString(), UserRole.USER.toString());
    }

    @Test
    void testGetAllPosts() throws Exception {
        Post post = Post.builder()
                .content("Test post content")
                .user(testUser)
                .isDeleted(false)
                .build();
        postRepository.save(post);

        mockMvc.perform(get(Routes.POSTS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].content").value("Test post content"));
    }

    @Test
    void testGetPostById() throws Exception {
        Post post = Post.builder()
                .content("Detailed post")
                .user(testUser)
                .isDeleted(false)
                .build();
        Post savedPost = postRepository.save(post);

        mockMvc.perform(get(Routes.POST_BY_ID.replace("{postId}", savedPost.getId().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Detailed post"));
    }

    @Test
    void testGetPostByIdNotFound() throws Exception {
        mockMvc.perform(get(Routes.POST_BY_ID.replace("{postId}", "999")))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetUserPosts() throws Exception {
        Post post1 = Post.builder()
                .content("User post 1")
                .user(testUser)
                .isDeleted(false)
                .build();
        Post post2 = Post.builder()
                .content("User post 2")
                .user(testUser)
                .isDeleted(false)
                .build();
        postRepository.save(post1);
        postRepository.save(post2);

        mockMvc.perform(get(Routes.USER_POSTS.replace("{userId}", testUser.getId().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetDeletedPostNotReturned() throws Exception {
        Post post = Post.builder()
                .content("Deleted post")
                .user(testUser)
                .isDeleted(true)
                .build();
        postRepository.save(post);

        mockMvc.perform(get(Routes.POSTS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testCreatePost() throws Exception {
        CreatePostRequest request = CreatePostRequest.builder()
                .content("Brand new post")
                .build();

        mockMvc.perform(post(Routes.POSTS)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Brand new post"))
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.likesCount").value(0));
    }

    @Test
    void testCreatePostUnauthenticatedRejected() throws Exception {
        CreatePostRequest request = CreatePostRequest.builder()
                .content("Should not be created")
                .build();

        mockMvc.perform(post(Routes.POSTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdatePostByOwner() throws Exception {
        Post post = postRepository.save(Post.builder()
                .content("Original content")
                .user(testUser)
                .isDeleted(false)
                .build());

        UpdatePostRequest request = UpdatePostRequest.builder()
                .content("Updated content")
                .build();

        mockMvc.perform(put(Routes.POST_BY_ID.replace("{postId}", post.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated content"));
    }

    @Test
    void testUpdatePostByNonOwnerForbidden() throws Exception {
        User otherUser = userRepository.save(User.builder()
                .email("other@example.com")
                .password("hashed")
                .username("otheruser")
                .firstName("Other")
                .lastName("User")
                .build());
        String otherToken = jwtTokenProvider.generateToken(otherUser.getEmail(), otherUser.getId().toString(), UserRole.USER.toString());

        Post post = postRepository.save(Post.builder()
                .content("Owned by testUser")
                .user(testUser)
                .isDeleted(false)
                .build());

        UpdatePostRequest request = UpdatePostRequest.builder()
                .content("Malicious update")
                .build();

        mockMvc.perform(put(Routes.POST_BY_ID.replace("{postId}", post.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeletePostByOwnerSoftDeletes() throws Exception {
        Post post = postRepository.save(Post.builder()
                .content("To be deleted")
                .user(testUser)
                .isDeleted(false)
                .build());

        mockMvc.perform(delete(Routes.POST_BY_ID.replace("{postId}", post.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(Routes.POSTS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testDeletePostByNonOwnerForbidden() throws Exception {
        User otherUser = userRepository.save(User.builder()
                .email("other2@example.com")
                .password("hashed")
                .username("otheruser2")
                .firstName("Other")
                .lastName("Two")
                .build());
        String otherToken = jwtTokenProvider.generateToken(otherUser.getEmail(), otherUser.getId().toString(), UserRole.USER.toString());

        Post post = postRepository.save(Post.builder()
                .content("Owned by testUser")
                .user(testUser)
                .isDeleted(false)
                .build());

        mockMvc.perform(delete(Routes.POST_BY_ID.replace("{postId}", post.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }
}
