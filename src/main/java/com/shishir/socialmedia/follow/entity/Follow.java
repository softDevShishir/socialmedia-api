package com.shishir.socialmedia.follow.entity;

import com.shishir.socialmedia.config.AuditData;
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
@Table(name = "follows",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_follow_follower_following", columnNames = {"follower_id", "following_id"})
        },
        indexes = {
                @Index(name = "idx_follow_follower_id", columnList = "follower_id"),
                @Index(name = "idx_follow_following_id", columnList = "following_id"),
                @Index(name = "idx_follow_follower_following", columnList = "follower_id,following_id")
        })
@Getter
@Setter
@ToString(exclude = {"follower", "following"})
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Follow extends AuditData {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;
}
