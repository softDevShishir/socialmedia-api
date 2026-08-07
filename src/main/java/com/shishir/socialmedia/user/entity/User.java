package com.shishir.socialmedia.user.entity;

import com.shishir.socialmedia.config.AuditData;
import com.shishir.socialmedia.follow.entity.Follow;
import com.shishir.socialmedia.post.entity.Post;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString(exclude = {"password", "posts", "followers", "following"})
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends AuditData {

    @Column(unique = true, nullable = false)
    @Email
    @NotBlank
    private String email;

    @Column(nullable = false)
    @NotBlank
    private String password;

    @Column(unique = true, nullable = false)
    @Size(min = 3, max = 50)
    @NotBlank
    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(length = 500)
    private String bio;

    @Column(name = "profile_image")
    private String profileImage;

    @Builder.Default
    @Column(name = "follower_count")
    private Integer followerCount = 0;

    @Builder.Default
    @Column(name = "following_count")
    private Integer followingCount = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> following = new ArrayList<>();
}
