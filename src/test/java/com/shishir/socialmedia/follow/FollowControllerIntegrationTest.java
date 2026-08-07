package com.shishir.socialmedia.follow;

import com.shishir.socialmedia.config.Routes;
import com.shishir.socialmedia.follow.entity.Follow;
import com.shishir.socialmedia.follow.repository.FollowRepository;
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
class FollowControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User user1;
    private User user2;
    private User user3;
    private String user1Token;

    @BeforeEach
    void setUp() {
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
    void testGetUserFollowers() throws Exception {
        Follow follow1 = Follow.builder()
                .follower(user1)
                .following(user2)
                .build();
        Follow follow2 = Follow.builder()
                .follower(user3)
                .following(user2)
                .build();
        followRepository.save(follow1);
        followRepository.save(follow2);

        mockMvc.perform(get(Routes.USER_FOLLOWERS.replace("{userId}", user2.getId().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetUserFollowing() throws Exception {
        Follow follow1 = Follow.builder()
                .follower(user1)
                .following(user2)
                .build();
        Follow follow2 = Follow.builder()
                .follower(user1)
                .following(user3)
                .build();
        followRepository.save(follow1);
        followRepository.save(follow2);

        mockMvc.perform(get(Routes.USER_FOLLOWING.replace("{userId}", user1.getId().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetUserFollowersEmpty() throws Exception {
        mockMvc.perform(get(Routes.USER_FOLLOWERS.replace("{userId}", user1.getId().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testCheckIfFollowing() throws Exception {
        Follow follow = Follow.builder()
                .follower(user1)
                .following(user2)
                .build();
        followRepository.save(follow);

        mockMvc.perform(get(Routes.FOLLOW_CHECK.replace("{userId}", user2.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.following").value(true))
                .andExpect(jsonPath("$.username").value("user2"));
    }

    @Test
    void testCheckIfNotFollowing() throws Exception {
        mockMvc.perform(get(Routes.FOLLOW_CHECK.replace("{userId}", user2.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.following").value(false));
    }

    @Test
    void testCheckIfFollowingUnauthenticatedRejected() throws Exception {
        mockMvc.perform(get(Routes.FOLLOW_CHECK.replace("{userId}", user2.getId().toString())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUniqueFollowConstraint() {
        Follow follow1 = Follow.builder()
                .follower(user1)
                .following(user2)
                .build();
        followRepository.save(follow1);

        Follow duplicateFollow = Follow.builder()
                .follower(user1)
                .following(user2)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> followRepository.save(duplicateFollow));
    }

    @Test
    void testMultipleUsersCanFollowSameUser() {
        Follow follow1 = Follow.builder()
                .follower(user1)
                .following(user2)
                .build();
        Follow follow2 = Follow.builder()
                .follower(user3)
                .following(user2)
                .build();

        followRepository.save(follow1);
        followRepository.save(follow2);

        assertEquals(2, followRepository.countByFollowingId(user2.getId()));
    }

    @Test
    void testFollowerAndFollowingCountUpdate() {
        Follow follow = Follow.builder()
                .follower(user1)
                .following(user2)
                .build();
        followRepository.save(follow);

        User updatedUser1 = userRepository.findById(user1.getId()).get();
        User updatedUser2 = userRepository.findById(user2.getId()).get();

        assertEquals(1, followRepository.countByFollowerId(user1.getId()));
        assertEquals(1, followRepository.countByFollowingId(user2.getId()));
    }

    @Test
    void testFollowUserIncrementsCountsOnBothUsers() throws Exception {
        mockMvc.perform(post(Routes.FOLLOW_USER.replace("{userId}", user2.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1Token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.action").value("FOLLOWED"))
                .andExpect(jsonPath("$.followerId").value(user1.getId()))
                .andExpect(jsonPath("$.followingId").value(user2.getId()));

        assertEquals(1, userRepository.findById(user2.getId()).orElseThrow().getFollowerCount());
        assertEquals(1, userRepository.findById(user1.getId()).orElseThrow().getFollowingCount());
    }

    @Test
    void testFollowUserUnauthenticatedRejected() throws Exception {
        mockMvc.perform(post(Routes.FOLLOW_USER.replace("{userId}", user2.getId().toString())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testFollowSelfRejected() throws Exception {
        mockMvc.perform(post(Routes.FOLLOW_USER.replace("{userId}", user1.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1Token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testFollowUserAlreadyFollowingConflict() throws Exception {
        mockMvc.perform(post(Routes.FOLLOW_USER.replace("{userId}", user2.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1Token))
                .andExpect(status().isCreated());

        mockMvc.perform(post(Routes.FOLLOW_USER.replace("{userId}", user2.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1Token))
                .andExpect(status().isConflict());
    }

    @Test
    void testUnfollowUserDecrementsCountsOnBothUsers() throws Exception {
        mockMvc.perform(post(Routes.FOLLOW_USER.replace("{userId}", user2.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1Token))
                .andExpect(status().isCreated());
        assertEquals(1, userRepository.findById(user2.getId()).orElseThrow().getFollowerCount());
        assertEquals(1, userRepository.findById(user1.getId()).orElseThrow().getFollowingCount());

        mockMvc.perform(delete(Routes.UNFOLLOW_USER.replace("{userId}", user2.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("UNFOLLOWED"));

        assertEquals(0, userRepository.findById(user2.getId()).orElseThrow().getFollowerCount());
        assertEquals(0, userRepository.findById(user1.getId()).orElseThrow().getFollowingCount());
    }

    @Test
    void testUnfollowUserNotFollowingNotFound() throws Exception {
        mockMvc.perform(delete(Routes.UNFOLLOW_USER.replace("{userId}", user2.getId().toString()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1Token))
                .andExpect(status().isNotFound());
    }
}
