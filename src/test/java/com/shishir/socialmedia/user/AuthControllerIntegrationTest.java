package com.shishir.socialmedia.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shishir.socialmedia.config.Routes;
import com.shishir.socialmedia.user.dto.UserLoginRequest;
import com.shishir.socialmedia.user.dto.UserRegisterRequest;
import com.shishir.socialmedia.user.entity.User;
import com.shishir.socialmedia.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testRegisterUser() throws Exception {
        UserRegisterRequest request = UserRegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .build();

        mockMvc.perform(post(Routes.AUTH_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testRegisterDuplicateEmail() throws Exception {
        User user = User.builder()
                .email("duplicate@example.com")
                .password(passwordEncoder.encode("password123"))
                .username("user1")
                .firstName("First")
                .lastName("User")
                .build();
        userRepository.save(user);

        UserRegisterRequest request = UserRegisterRequest.builder()
                .email("duplicate@example.com")
                .password("password123")
                .username("differentuser")
                .firstName("Different")
                .lastName("User")
                .build();

        mockMvc.perform(post(Routes.AUTH_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void testLoginSuccessful() throws Exception {
        User user = User.builder()
                .email("login@example.com")
                .password(passwordEncoder.encode("password123"))
                .username("loginuser")
                .firstName("Login")
                .lastName("User")
                .build();
        userRepository.save(user);

        UserLoginRequest request = UserLoginRequest.builder()
                .email("login@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post(Routes.AUTH_LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("loginuser"));
    }

    @Test
    void testLoginInvalidPassword() throws Exception {
        User user = User.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .build();
        userRepository.save(user);

        UserLoginRequest request = UserLoginRequest.builder()
                .email("test@example.com")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post(Routes.AUTH_LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
