package com.shishir.socialmedia.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shishir.socialmedia.comment.dto.CreateCommentRequest;
import com.shishir.socialmedia.comment.dto.UpdateCommentRequest;
import com.shishir.socialmedia.comment.entity.Comment;
import com.shishir.socialmedia.comment.repository.CommentRepository;
import com.shishir.socialmedia.config.Routes;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CommentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Post testPost;
    private String authToken;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .email("commenttest@example.com")
                .password("hashed")
                .username("commentuser")
                .firstName("Comment")
                .lastName("User")
                .build();
        testUser = userRepository.save(testUser);

        authToken = jwtTokenProvider.generateToken(testUser.getEmail(), testUser.getId().toString(), UserRole.USER.toString());

        testPost = Post.builder()
                .content("Test post for comments")
                .user(testUser)
                .isDeleted(false)
                .build();
        testPost = postRepository.save(testPost);
    }

    @Test
    void testGetPostComments() throws Exception {
        Comment comment1 = Comment.builder()
                .content("First comment")
                .post(testPost)
                .user(testUser)
                .isDeleted(false)
                .build();
        Comment comment2 = Comment.builder()
                .content("Second comment")
                .post(testPost)
                .user(testUser)
                .isDeleted(false)
                .build();
        commentRepository.save(comment1);
        commentRepository.save(comment2);

        mockMvc.perform(get(Routes.POST_COMMENTS.replace("{postId}", testPost.getId().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetCommentById() throws Exception {
        Comment comment = Comment.builder()
                .content("Test comment")
                .post(testPost)
                .user(testUser)
                .isDeleted(false)
                .build();
        Comment savedComment = commentRepository.save(comment);

        mockMvc.perform(get(Routes.COMMENT_BY_ID.replace("{commentId}", savedComment.getId().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Test comment"));
    }

    @Test
    void testGetCommentByIdNotFound() throws Exception {
        mockMvc.perform(get(Routes.COMMENT_BY_ID.replace("{commentId}", "999")))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetDeletedCommentNotReturned() throws Exception {
        Comment comment = Comment.builder()
                .content("Deleted comment")
                .post(testPost)
                .user(testUser)
                .isDeleted(true)
                .build();
        commentRepository.save(comment);

        mockMvc.perform(get(Routes.POST_COMMENTS.replace("{postId}", testPost.getId().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testCommentCountIncrementOnPostComments() throws Exception {
        Comment comment1 = Comment.builder()
                .content("Comment 1")
                .post(testPost)
                .user(testUser)
                .isDeleted(false)
                .build();
        Comment comment2 = Comment.builder()
                .content("Comment 2")
                .post(testPost)
                .user(testUser)
                .isDeleted(false)
                .build();
        commentRepository.save(comment1);
        commentRepository.save(comment2);

        assertEquals(2, commentRepository.countByPostId(testPost.getId()));
    }

    @Test
    void testCreateCommentIncrementsPostCommentsCount() throws Exception {
        assertEquals(0, postRepository.findById(testPost.getId()).orElseThrow().getCommentsCount());

        CreateCommentRequest request = CreateCommentRequest.builder()
                .content("A brand new comment")
                .build();

        mockMvc.perform(post(Routes.POST_COMMENTS.replace("{postId}", testPost.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("A brand new comment"))
                .andExpect(jsonPath("$.postId").value(testPost.getId()))
                .andExpect(jsonPath("$.userId").value(testUser.getId()));

        Post updatedPost = postRepository.findById(testPost.getId()).orElseThrow();
        assertEquals(1, updatedPost.getCommentsCount());
    }

    @Test
    void testCreateCommentUnauthenticatedRejected() throws Exception {
        CreateCommentRequest request = CreateCommentRequest.builder()
                .content("Should not be created")
                .build();

        mockMvc.perform(post(Routes.POST_COMMENTS.replace("{postId}", testPost.getId().toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateCommentByOwner() throws Exception {
        Comment comment = commentRepository.save(Comment.builder()
                .content("Original content")
                .post(testPost)
                .user(testUser)
                .isDeleted(false)
                .build());

        UpdateCommentRequest request = UpdateCommentRequest.builder()
                .content("Updated content")
                .build();

        mockMvc.perform(put(Routes.COMMENT_BY_ID.replace("{commentId}", comment.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated content"));
    }

    @Test
    void testUpdateCommentByNonOwnerForbidden() throws Exception {
        User otherUser = userRepository.save(User.builder()
                .email("other@example.com")
                .password("hashed")
                .username("otheruser")
                .firstName("Other")
                .lastName("User")
                .build());
        String otherToken = jwtTokenProvider.generateToken(otherUser.getEmail(), otherUser.getId().toString(), UserRole.USER.toString());

        Comment comment = commentRepository.save(Comment.builder()
                .content("Owned by testUser")
                .post(testPost)
                .user(testUser)
                .isDeleted(false)
                .build());

        UpdateCommentRequest request = UpdateCommentRequest.builder()
                .content("Malicious update")
                .build();

        mockMvc.perform(put(Routes.COMMENT_BY_ID.replace("{commentId}", comment.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteCommentByOwnerDecrementsPostCommentsCount() throws Exception {
        CreateCommentRequest createRequest = CreateCommentRequest.builder()
                .content("To be deleted")
                .build();

        String createResponse = mockMvc.perform(post(Routes.POST_COMMENTS.replace("{postId}", testPost.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long commentId = objectMapper.readTree(createResponse).get("id").asLong();

        assertEquals(1, postRepository.findById(testPost.getId()).orElseThrow().getCommentsCount());

        mockMvc.perform(delete(Routes.COMMENT_BY_ID.replace("{commentId}", commentId.toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken))
                .andExpect(status().isNoContent());

        Post updatedPost = postRepository.findById(testPost.getId()).orElseThrow();
        assertEquals(0, updatedPost.getCommentsCount());

        mockMvc.perform(get(Routes.COMMENT_BY_ID.replace("{commentId}", commentId.toString())))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteCommentByNonOwnerForbidden() throws Exception {
        User otherUser = userRepository.save(User.builder()
                .email("other2@example.com")
                .password("hashed")
                .username("otheruser2")
                .firstName("Other")
                .lastName("Two")
                .build());
        String otherToken = jwtTokenProvider.generateToken(otherUser.getEmail(), otherUser.getId().toString(), UserRole.USER.toString());

        Comment comment = commentRepository.save(Comment.builder()
                .content("Owned by testUser")
                .post(testPost)
                .user(testUser)
                .isDeleted(false)
                .build());

        mockMvc.perform(delete(Routes.COMMENT_BY_ID.replace("{commentId}", comment.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }
}
