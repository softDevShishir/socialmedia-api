package com.shishir.socialmedia.like.repository;

import com.shishir.socialmedia.like.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPostIdAndUserId(Long postId, Long userId);

    List<Like> findByPostIdOrderByCreatedAtDesc(Long postId);

    List<Like> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByPostId(Long postId);

    long countByUserId(Long userId);

    boolean existsByPostIdAndUserId(Long postId, Long userId);
}
