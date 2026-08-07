package com.shishir.socialmedia.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shishir.socialmedia.exception.ExceptionResponse;
import com.shishir.socialmedia.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.POST, Routes.AUTH_REGISTER, Routes.AUTH_LOGIN).permitAll()
                        .requestMatchers(HttpMethod.GET, Routes.USERS, Routes.USER_BY_USERNAME).permitAll()
                        .requestMatchers(HttpMethod.GET, Routes.POSTS, Routes.POST_BY_ID, Routes.USER_POSTS).permitAll()
                        .requestMatchers(HttpMethod.GET, Routes.POST_COMMENTS, Routes.COMMENT_BY_ID).permitAll()
                        .requestMatchers(HttpMethod.GET, Routes.POST_LIKES).permitAll()
                        .requestMatchers(HttpMethod.GET, Routes.USER_FOLLOWERS, Routes.USER_FOLLOWING).permitAll()
                        .requestMatchers(HttpMethod.GET, Routes.TIMELINE, Routes.EXPLORE).permitAll()
                        .requestMatchers(HttpMethod.GET, Routes.SEARCH_USERS, Routes.SEARCH_POSTS).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("Unauthorized access to {}: {}", request.getRequestURI(), authException.getMessage());
                            response.setStatus(401);
                            response.setContentType("application/json");
                            ExceptionResponse errorResponse = ExceptionResponse.builder()
                                    .timestamp(LocalDateTime.now())
                                    .status(401)
                                    .error("UNAUTHORIZED")
                                    .message("Unauthorized access")
                                    .path(request.getRequestURI())
                                    .build();
                            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.warn("Access denied to {}: {}", request.getRequestURI(), accessDeniedException.getMessage());
                            response.setStatus(403);
                            response.setContentType("application/json");
                            ExceptionResponse errorResponse = ExceptionResponse.builder()
                                    .timestamp(LocalDateTime.now())
                                    .status(403)
                                    .error("FORBIDDEN")
                                    .message("Access denied")
                                    .path(request.getRequestURI())
                                    .build();
                            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                        })
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
