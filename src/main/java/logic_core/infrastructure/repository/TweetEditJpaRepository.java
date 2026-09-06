package logic_core.infrastructure.repository;

import logic_core.infrastructure.persistence.entity.tweetedit.TweetEditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TweetEditJpaRepository extends JpaRepository<TweetEditEntity, UUID> {

    @Query("SELECT te FROM TweetEditEntity te WHERE te.tweet.id = :tweetId AND te.tweet.isDeleted = false ORDER BY te.createdAt DESC")
    List<TweetEditEntity> findHistoryByTweetId(@Param("tweetId") UUID tweetId);

    /**
     * Hard-deletes all edit-history records belonging to a tweet.
     * Used during tweet deletion to cascade-delete related edit history.
     */
    @Modifying
    @Query("DELETE FROM TweetEditEntity te WHERE te.tweet.id = :tweetId")
    void deleteByTweetId(@Param("tweetId") UUID tweetId);
}
