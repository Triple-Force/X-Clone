package logic_core.infrastructure.repository;

import logic_core.domain.model.TweetEditHistory;
import logic_core.domain.repository.TweetEditRepository;
import logic_core.infrastructure.persistence.entity.tweetedit.TweetEditEntity;
import logic_core.infrastructure.persistence.entity.tweet.TweetEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Encapsulates all TweetEdit persistence operations.
 * <p>
 * {@code TweetRepositoryAdapter} delegates TweetEdit-related persistence to this
 * adapter, keeping the tweet and tweet-edit persistence concerns separated while
 * the TweetRepository contract continues to expose edit-history methods.
 */
@Component
@Transactional
public class TweetEditRepositoryAdapter implements TweetEditRepository {

    private final TweetEditJpaRepository tweetEditJpaRepository;
    private final TweetJpaRepository tweetJpaRepository;

    public TweetEditRepositoryAdapter(TweetEditJpaRepository tweetEditJpaRepository,
                                      TweetJpaRepository tweetJpaRepository) {
        this.tweetEditJpaRepository = tweetEditJpaRepository;
        this.tweetJpaRepository = tweetJpaRepository;
    }

    /**
     * Persists a new edit-history entry for the given tweet.
     *
     * @param tweetId        the tweet whose content was changed
     * @param previousContent the content before the edit
     */
    @Override
    public void appendEditHistory(UUID tweetId, String previousContent) {
        TweetEntity tweet = tweetJpaRepository.getReferenceById(tweetId);
        TweetEditEntity entity = new TweetEditEntity();
        entity.setTweet(tweet);
        entity.setPreviousContent(previousContent);
        tweetEditJpaRepository.save(entity);
    }

    /**
     * Returns the edit-history for a given tweet, ordered by creation time descending.
     *
     * @param tweetId the tweet whose history to retrieve
     * @return list of edit-history entries (newest first)
     */
    @Override
    public List<TweetEditHistory> findEditHistoryByTweetId(UUID tweetId) {
        return tweetEditJpaRepository.findHistoryByTweetId(tweetId)
                .stream()
                .map(e -> new TweetEditHistory(
                        e.getId(),
                        e.getPreviousContent(),
                        e.getCreatedAt()
                ))
                .toList();
    }

    /**
     * Hard-deletes all edit-history records belonging to a tweet.
     * Used during tweet deletion to cascade-delete related edit history.
     *
     * @param tweetId the tweet whose edit history should be deleted
     */
    @Override
    public void deleteByTweetId(UUID tweetId) {
        tweetEditJpaRepository.deleteByTweetId(tweetId);
    }
}
