package com.shishir.socialmedia.search;

import com.shishir.socialmedia.config.Routes;
import com.shishir.socialmedia.post.entity.Post;
import com.shishir.socialmedia.post.repository.PostRepository;
import com.shishir.socialmedia.user.entity.User;
import com.shishir.socialmedia.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class SearchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        userRepository.deleteAll();

        user1 = User.builder()
                .email("john@example.com")
                .password("hashed")
                .username("john_doe")
                .firstName("John")
                .lastName("Doe")
                .bio("Tech enthusiast")
                .isActive(true)
                .build();
        userRepository.save(user1);

        user2 = User.builder()
                .email("jane@example.com")
                .password("hashed")
                .username("jane_smith")
                .firstName("Jane")
                .lastName("Smith")
                .bio("Designer and creator")
                .isActive(true)
                .build();
        userRepository.save(user2);

        user3 = User.builder()
                .email("bob@example.com")
                .password("hashed")
                .username("bob_johnson")
                .firstName("Bob")
                .lastName("Johnson")
                .bio("Software engineer")
                .isActive(false)
                .build();
        userRepository.save(user3);
    }

    @Test
    void testSearchUsersByUsername() throws Exception {
        mockMvc.perform(get(Routes.SEARCH_USERS + "?query=john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.length()").value(1))
                .andExpect(jsonPath("$.results[0].username").value("john_doe"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testSearchUsersPartialMatch() throws Exception {
        mockMvc.perform(get(Routes.SEARCH_USERS + "?query=doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.length()").value(1))
                .andExpect(jsonPath("$.results[0].username").value("john_doe"));
    }

    @Test
    void testSearchUsersCaseInsensitive() throws Exception {
        mockMvc.perform(get(Routes.SEARCH_USERS + "?query=JANE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.length()").value(1))
                .andExpect(jsonPath("$.results[0].username").value("jane_smith"));
    }

    @Test
    void testSearchUsersMultipleResults() throws Exception {
        mockMvc.perform(get(Routes.SEARCH_USERS + "?query=smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.length()").value(1));
    }

    @Test
    void testSearchUsersNoResults() throws Exception {
        mockMvc.perform(get(Routes.SEARCH_USERS + "?query=nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void testSearchUsersExcludesInactiveUsers() throws Exception {
        mockMvc.perform(get(Routes.SEARCH_USERS + "?query=johnson"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.length()").value(0));
    }

    @Test
    void testSearchPostsByContent() throws Exception {
        Post post1 = Post.builder()
                .content("Spring Boot is awesome for backend development")
                .user(user1)
                .isDeleted(false)
                .build();
        Post post2 = Post.builder()
                .content("Java development tips and tricks")
                .user(user2)
                .isDeleted(false)
                .build();
        postRepository.save(post1);
        postRepository.save(post2);

        mockMvc.perform(get(Routes.SEARCH_POSTS + "?query=Spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.length()").value(1))
                .andExpect(jsonPath("$.results[0].content").value("Spring Boot is awesome for backend development"));
    }

    @Test
    void testSearchPostsCaseInsensitive() throws Exception {
        Post post = Post.builder()
                .content("Building REST APIs with Spring Boot")
                .user(user1)
                .isDeleted(false)
                .build();
        postRepository.save(post);

        mockMvc.perform(get(Routes.SEARCH_POSTS + "?query=rest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.length()").value(1));
    }

    @Test
    void testSearchPostsExcludesDeletedPosts() throws Exception {
        Post post1 = Post.builder()
                .content("Active post about testing")
                .user(user1)
                .isDeleted(false)
                .build();
        Post post2 = Post.builder()
                .content("Deleted post about testing")
                .user(user2)
                .isDeleted(true)
                .build();
        postRepository.save(post1);
        postRepository.save(post2);

        mockMvc.perform(get(Routes.SEARCH_POSTS + "?query=testing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.length()").value(1));
    }

    @Test
    void testSearchWithPagination() throws Exception {
        for (int i = 0; i < 15; i++) {
            Post post = Post.builder()
                    .content("Post about testing number " + i)
                    .user(i % 2 == 0 ? user1 : user2)
                    .isDeleted(false)
                    .build();
            postRepository.save(post);
        }

        mockMvc.perform(get(Routes.SEARCH_POSTS + "?query=testing&page=0&pageSize=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    void testSearchWithEmptyQuery() throws Exception {
        mockMvc.perform(get(Routes.SEARCH_USERS + "?query="))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSearchWithoutQuery() throws Exception {
        mockMvc.perform(get(Routes.SEARCH_USERS))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSearchWithInvalidPageSize() throws Exception {
        mockMvc.perform(get(Routes.SEARCH_USERS + "?query=test&pageSize=100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSearchByNameExcludesInactiveUsersDespiteFirstNameMatch() {
        // Regression test for the AND/OR grouping bug in the derived query name:
        // "...OrLastNameContainingIgnoreCaseAndIsActiveTrue" parses left-to-right with no
        // grouping, so a naive derived-name implementation would match on
        // "firstName LIKE %x% OR (lastName LIKE %x% AND isActive = true)" -- silently
        // including inactive users whenever their first name matches. The repository
        // method uses an explicit @Query with parentheses to avoid this.
        assertEquals("Bob", user3.getFirstName());
        assertEquals(Boolean.FALSE, user3.getIsActive());

        var results = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseAndIsActiveTrue(
                "Bob", "Bob", PageRequest.of(0, 10));

        assertEquals(0, results.getTotalElements());
    }
}
