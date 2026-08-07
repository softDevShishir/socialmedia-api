package com.shishir.socialmedia.post.repository;

import com.shishir.socialmedia.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId);

    List<Post> findByIsDeletedFalseOrderByCreatedAtDesc();

    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserId(Long userId);

    boolean existsByIdAndUserId(Long postId, Long userId);

    Page<Post> findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc(List<Long> userIds, Pageable pageable);

    Page<Post> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Post> findByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    @Modifying
    @Query("UPDATE Post p SET p.commentsCount = p.commentsCount + 1 WHERE p.id = :postId")
    void incrementCommentsCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.commentsCount = GREATEST(p.commentsCount - 1, 0) WHERE p.id = :postId")
    void decrementCommentsCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.likesCount = p.likesCount + 1 WHERE p.id = :postId")
    void incrementLikesCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.likesCount = GREATEST(p.likesCount - 1, 0) WHERE p.id = :postId")
    void decrementLikesCount(@Param("postId") Long postId);

    Page<Post> findByContentContainingIgnoreCaseAndIsDeletedFalse(String content, Pageable pageable);

    Page<Post> findByUserUsernameContainingIgnoreCaseAndIsDeletedFalse(String username, Pageable pageable);
}
