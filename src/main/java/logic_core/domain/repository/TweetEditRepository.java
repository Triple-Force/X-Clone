package logic_core.domain.repository;

import logic_core.domain.model.TweetEditHistory;

import java.util.List;
import java.util.UUID;

/**
 * Domain repository for tweet edit-history persistence.
 * Each operation owns only its own persistence responsibility.
 */
public interface TweetEditRepository
{
    /**
     * Persists a new edit-history entry for the given tweet.
     */
    void appendEditHistory(UUID tweetId, String previousContent);

    /**
     * Returns the edit-history for a given tweet, ordered by creation time descending.
     */
    List<TweetEditHistory> findEditHistoryByTweetId(UUID tweetId);

    /**
     * Hard-deletes all edit-history records belonging to a tweet.
     * Used during tweet deletion to cascade-delete related edit history.
     */
    void deleteByTweetId(UUID tweetId);
}
