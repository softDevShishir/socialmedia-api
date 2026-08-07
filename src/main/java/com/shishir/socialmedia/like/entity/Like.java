package com.shishir.socialmedia.like.entity;

import com.shishir.socialmedia.config.AuditData;
import com.shishir.socialmedia.post.entity.Post;
import com.shishir.socialmedia.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "likes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_like_post_user", columnNames = {"post_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_like_post_id", columnList = "post_id"),
                @Index(name = "idx_like_user_id", columnList = "user_id"),
                @Index(name = "idx_like_post_user", columnList = "post_id,user_id")
        })
@Getter
@Setter
@ToString(exclude = {"post", "user"})
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Like extends AuditData {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
