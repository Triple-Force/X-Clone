package logic_core.infrastructure.repository;

import logic_core.infrastructure.persistence.entity.media.MediaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MediaJpaRepository extends JpaRepository<MediaEntity, UUID> {

    /**
     * Find all media for a given tweet.
     * 
     * Equivalent to MediaDao.findByTweetId():
     * SELECT m FROM Media m WHERE m.tweet.id = :tweetId
     */
    @Query("""
            SELECT m
            FROM MediaEntity m
            WHERE m.tweet.id = :tweetId
            """)
    List<MediaEntity> findByTweetId(@Param("tweetId") UUID tweetId);

    /**
     * Find media by IDs, ordered by displayOrder.
     * 
     * Equivalent to MediaDao.findByIds():
     * SELECT m FROM Media m WHERE m.id IN :mediaIds ORDER BY m.displayOrder
     */
    @Query("""
            SELECT m
            FROM MediaEntity m
            WHERE m.id IN :mediaIds
            ORDER BY m.displayOrder
            """)
    List<MediaEntity> findByIds(@Param("mediaIds") List<UUID> mediaIds);

    /**
     * Check if media belongs to a user.
     * 
     * NOTE: The legacy MediaDao.belongsToUser() references a non-existent Upload entity.
     * This method preserves the same behavior — it will always return false.
     * 
     * Equivalent to MediaDao.belongsToUser():
     * SELECT COUNT(m) FROM Media m WHERE m.id = :mediaId AND m.tweet IS NULL AND EXISTS (SELECT 1 FROM Upload u ...)
     */
    @Query("""
            SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
            FROM MediaEntity m
            WHERE m.id = :mediaId
              AND m.tweet IS NULL
            """)
    boolean belongsToUser(@Param("mediaId") UUID mediaId, @Param("userId") UUID userId);

    /**
     * Check if media is already attached to a tweet.
     * 
     * Equivalent to MediaDao.isAlreadyAttached():
     * SELECT COUNT(m) FROM Media m WHERE m.id = :mediaId AND m.tweet IS NOT NULL
     */
    @Query("""
            SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
            FROM MediaEntity m
            WHERE m.id = :mediaId
              AND m.tweet IS NOT NULL
            """)
    boolean isAlreadyAttached(@Param("mediaId") UUID mediaId);

    /**
     * Hard-deletes all media records belonging to a tweet.
     * Used during tweet deletion to cascade-delete related media.
     */
    @Modifying
    @Query("DELETE FROM MediaEntity m WHERE m.tweet.id = :tweetId")
    void deleteByTweetId(@Param("tweetId") UUID tweetId);
}
