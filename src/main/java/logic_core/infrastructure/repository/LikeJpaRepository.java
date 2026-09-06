package logic_core.infrastructure.repository;

import logic_core.infrastructure.persistence.entity.like.LikeEntity;
import logic_core.infrastructure.persistence.entity.like.LikeEntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LikeJpaRepository extends JpaRepository<LikeEntity, LikeEntityId> {

    /**
     * Check if a like relation exists between a user and a tweet.
     * 
     * Equivalent to LikeDao.findRelation():
     * WHERE l.user.id = :userId
     *   AND l.tweet.id = :tweetId
     *   AND l.user.isDeleted = false
     *   AND l.tweet.isDeleted = false
     * 
     * Note: Like has soft-delete filtering for both user and tweet.
     * 
     * @return count of matching likes (0 or 1)
     */
    @Query("""
            SELECT COUNT(l)
            FROM LikeEntity l
            WHERE l.user.id = :userId
              AND l.tweet.id = :tweetId
              AND l.user.isDeleted = false
              AND l.tweet.isDeleted = false
            """)
    long countByUserAndTweet(
            @Param("userId") UUID userId,
            @Param("tweetId") UUID tweetId
    );

    /**
     * Find all likes for a given tweet.
     * 
     * Equivalent to LikeDao.findByTweetId():
     * WHERE l.tweet.id = :tweetId
     *   AND l.tweet.isDeleted = false
     *   AND l.user.isDeleted = false
     * 
     * Note: Like has soft-delete filtering for both user and tweet.
     */
    @Query("""
            SELECT l
            FROM LikeEntity l
            WHERE l.tweet.id = :tweetId
              AND l.tweet.isDeleted = false
              AND l.user.isDeleted = false
            """)
    List<LikeEntity> findByTweetId(@Param("tweetId") UUID tweetId);

    /**
     * Count likes for a given tweet.
     * 
     * Equivalent to LikeDao.countLikesByTweetId():
     * WHERE l.tweet.id = :tweetId
     *   AND l.tweet.isDeleted = false
     *   AND l.user.isDeleted = false
     * 
     * Note: Like has soft-delete filtering for both user and tweet.
     */
    @Query("""
            SELECT COUNT(l)
            FROM LikeEntity l
            WHERE l.tweet.id = :tweetId
              AND l.tweet.isDeleted = false
              AND l.user.isDeleted = false
            """)
    long countLikesByTweetId(@Param("tweetId") UUID tweetId);

    /**
     * Hard-deletes all like records belonging to a tweet.
     * Used during tweet deletion to cascade-delete related likes.
     */
    @Modifying
    @Query("DELETE FROM LikeEntity l WHERE l.tweet.id = :tweetId")
    void deleteByTweetId(@Param("tweetId") UUID tweetId);
}
