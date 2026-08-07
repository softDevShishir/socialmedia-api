package com.shishir.socialmedia.comment.repository;

import com.shishir.socialmedia.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdAndIsDeletedFalseOrderByCreatedAtDesc(Long postId);

    List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId);

    List<Comment> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId);

    long countByPostId(Long postId);

    long countByUserId(Long userId);

    boolean existsByIdAndUserId(Long commentId, Long userId);
}
