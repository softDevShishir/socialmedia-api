package com.shishir.socialmedia.post.repository;

import com.shishir.socialmedia.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId);

    List<Post> findByIsDeletedFalseOrderByCreatedAtDesc();

    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserId(Long userId);

    boolean existsByIdAndUserId(Long postId, Long userId);
}
