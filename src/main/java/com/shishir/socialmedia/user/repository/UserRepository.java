package com.shishir.socialmedia.user.repository;

import com.shishir.socialmedia.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    List<User> findByUsernameContainingIgnoreCase(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Modifying
    @Query("UPDATE User u SET u.followerCount = u.followerCount + 1 WHERE u.id = :userId")
    void incrementFollowerCount(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.followerCount = GREATEST(u.followerCount - 1, 0) WHERE u.id = :userId")
    void decrementFollowerCount(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.followingCount = u.followingCount + 1 WHERE u.id = :userId")
    void incrementFollowingCount(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.followingCount = GREATEST(u.followingCount - 1, 0) WHERE u.id = :userId")
    void decrementFollowingCount(@Param("userId") Long userId);

    Page<User> findByUsernameContainingIgnoreCaseAndIsActiveTrue(String username, Pageable pageable);

    @Query("SELECT u FROM User u WHERE (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) "
            + "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND u.isActive = true")
    Page<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseAndIsActiveTrue(
            @Param("firstName") String firstName, @Param("lastName") String lastName, Pageable pageable);

    Page<User> findByBioContainingIgnoreCaseAndIsActiveTrue(String bio, Pageable pageable);
}
