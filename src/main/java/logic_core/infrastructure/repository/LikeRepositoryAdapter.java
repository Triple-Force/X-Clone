package logic_core.infrastructure.repository;

import logic_core.domain.model.LikeRelation;
import logic_core.infrastructure.mapper.LikeEntityMapper;
import logic_core.infrastructure.persistence.entity.like.LikeEntity;
import logic_core.infrastructure.persistence.entity.like.LikeEntityId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Infrastructure-level adapter for Like persistence operations.
 * <p>
 * Uses {@link LikeJpaRepository}, {@link UserJpaRepository}, and {@link TweetJpaRepository}
 * for Spring Data JPA operations, and {@link LikeEntityMapper} for entity/domain mapping.
 * <p>
 * This adapter will be used by {@code RelationshipRepositoryAdapter} to
 * replace the legacy {@code LikeDao} dependency.
 */
@Component
@Transactional
public class LikeRepositoryAdapter {

    private final LikeJpaRepository likeJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final TweetJpaRepository tweetJpaRepository;

    public LikeRepositoryAdapter(LikeJpaRepository likeJpaRepository,
                                  UserJpaRepository userJpaRepository,
                                  TweetJpaRepository tweetJpaRepository) {
        this.likeJpaRepository = likeJpaRepository;
        this.userJpaRepository = userJpaRepository;
        this.tweetJpaRepository = tweetJpaRepository;
    }

    /**
     * Persists a new like relation.
     *
     * @param like the domain like relation to persist
     */
    public void saveLike(LikeRelation like) {
        var user = userJpaRepository.getReferenceById(like.getUserId());
        var tweet = tweetJpaRepository.getReferenceById(like.getTweetId());
        LikeEntity entity = LikeEntityMapper.toPersistence(like, user, tweet);
        likeJpaRepository.save(entity);
    }

    /**
     * Hard-deletes an existing like relation.
     *
     * @param like the domain like relation to delete
     */
    public void deleteLike(LikeRelation like) {
        LikeEntityId id = new LikeEntityId(
                like.getUserId(),
                like.getTweetId()
        );
        likeJpaRepository.deleteById(id);
    }

    /**
     * Check if a like relation exists between a user and a tweet.
     * Filters out soft-deleted users and tweets.
     * 
     * Equivalent to LikeDao.findRelation() behavior.
     *
     * @param userId the user's ID
     * @param tweetId the tweet's ID
     * @return true if the like relation exists
     */
    public boolean hasLiked(UUID userId, UUID tweetId) {
        return likeJpaRepository.countByUserAndTweet(userId, tweetId) > 0;
    }

    /**
     * Check if a like relation exists between a user and a tweet (Boolean wrapper).
     * Filters out soft-deleted users and tweets.
     * 
     * Equivalent to LikeDao.findRelation() behavior.
     *
     * @param userId the user's ID
     * @param tweetId the tweet's ID
     * @return Boolean indicating if the like relation exists
     */
    public Boolean existsLikeRelation(UUID userId, UUID tweetId) {
        return hasLiked(userId, tweetId);
    }

    /**
     * Find all likes for a given tweet.
     * Filters out soft-deleted users and tweets.
     * 
     * Equivalent to LikeDao.findByTweetId() behavior.
     *
     * @param tweetId the tweet's ID
     * @return list of like relations
     */
    public List<LikeRelation> findLikesByTweetId(UUID tweetId) {
        return likeJpaRepository.findByTweetId(tweetId)
                .stream()
                .map(LikeEntityMapper::toDomain)
                .toList();
    }

    /**
     * Count likes for a given tweet.
     * Filters out soft-deleted users and tweets.
     * 
     * Equivalent to LikeDao.countLikesByTweetId() behavior.
     *
     * @param tweetId the tweet's ID
     * @return the like count
     */
    public long countLikesByTweetId(UUID tweetId) {
        return likeJpaRepository.countLikesByTweetId(tweetId);
    }

    /**
     * Hard-deletes all like records belonging to a tweet.
     * Used during tweet deletion to cascade-delete related likes.
     *
     * @param tweetId the tweet whose likes should be deleted
     */
    public void deleteByTweetId(UUID tweetId) {
        likeJpaRepository.deleteByTweetId(tweetId);
    }
}
