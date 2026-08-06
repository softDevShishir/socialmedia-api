package com.shishir.socialmedia.user;

import com.shishir.socialmedia.config.Routes;
import com.shishir.socialmedia.user.entity.User;
import com.shishir.socialmedia.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testGetAllUsers() throws Exception {
        User user1 = User.builder()
                .email("user1@example.com")
                .password("hashed")
                .username("user1")
                .firstName("User")
                .lastName("One")
                .isActive(true)
                .build();
        userRepository.save(user1);

        mockMvc.perform(get(Routes.USERS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetUserByUsername() throws Exception {
        User user = User.builder()
                .email("profile@example.com")
                .password("hashed")
                .username("profileuser")
                .firstName("Profile")
                .lastName("User")
                .build();
        userRepository.save(user);

        mockMvc.perform(get(Routes.USER_BY_USERNAME.replace("{username}", "profileuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("profileuser"));
    }

    @Test
    void testGetUserByUsernameNotFound() throws Exception {
        mockMvc.perform(get(Routes.USER_BY_USERNAME.replace("{username}", "nonexistent")))
                .andExpect(status().isNotFound());
    }
}
