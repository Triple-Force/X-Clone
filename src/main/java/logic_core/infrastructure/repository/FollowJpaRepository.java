package logic_core.infrastructure.repository;

import logic_core.infrastructure.persistence.entity.follow.FollowEntity;
import logic_core.infrastructure.persistence.entity.follow.FollowEntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowJpaRepository extends JpaRepository<FollowEntity, FollowEntityId> {

    /**
     * Find a specific follow relation between two users.
     *
     * Equivalent to FollowDao.findRelation():
     * WHERE f.follower.id = :followerId
     *   AND f.following.id = :followingId
     *   AND f.follower.isDeleted = false
     *   AND f.following.isDeleted = false
     */
    @Query("""
            SELECT f
            FROM FollowEntity f
            WHERE f.follower.id = :followerId
              AND f.following.id = :followingId
              AND f.follower.isDeleted = false
              AND f.following.isDeleted = false
            """)
    Optional<FollowEntity> findRelation(
            @Param("followerId") UUID followerId,
            @Param("followingId") UUID followingId
    );

    /**
     * Find all users that followerId is following.
     *
     * Equivalent to FollowDao.findByFollowerId():
     * WHERE f.follower.id = :followerId
     *   AND f.follower.isDeleted = false
     *   AND f.following.isDeleted = false
     */
    @Query("""
            SELECT f
            FROM FollowEntity f
            WHERE f.follower.id = :followerId
              AND f.follower.isDeleted = false
              AND f.following.isDeleted = false
            """)
    List<FollowEntity> findByFollowerId(@Param("followerId") UUID followerId);

    /**
     * Find all followers of a user (users where followingId = :followingId).
     *
     * Equivalent to FollowDao.findByFollowingId():
     * WHERE f.following.id = :followingId
     *   AND f.follower.isDeleted = false
     *   AND f.following.isDeleted = false
     */
    @Query("""
            SELECT f
            FROM FollowEntity f
            WHERE f.following.id = :followingId
              AND f.follower.isDeleted = false
              AND f.following.isDeleted = false
            """)
    List<FollowEntity> findByFollowingId(@Param("followingId") UUID followingId);

    /**
     * Count how many users follow the given user.
     *
     * Equivalent to FollowDao.countFollowers():
     * WHERE f.following.id = :userId
     *   AND f.follower.isDeleted = false
     *   AND f.following.isDeleted = false
     */
    @Query("""
            SELECT COUNT(f)
            FROM FollowEntity f
            WHERE f.following.id = :userId
              AND f.follower.isDeleted = false
              AND f.following.isDeleted = false
            """)
    long countFollowers(@Param("userId") UUID userId);

    /**
     * Count how many users the given user follows.
     *
     * Equivalent to FollowDao.countFollowing():
     * WHERE f.follower.id = :userId
     *   AND f.follower.isDeleted = false
     *   AND f.following.isDeleted = false
     */
    @Query("""
            SELECT COUNT(f)
            FROM FollowEntity f
            WHERE f.follower.id = :userId
              AND f.follower.isDeleted = false
              AND f.following.isDeleted = false
            """)
    long countFollowing(@Param("userId") UUID userId);
}
