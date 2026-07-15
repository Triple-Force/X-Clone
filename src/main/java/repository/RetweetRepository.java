package repository;

import model.Retweet;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RetweetRepository
{
    Retweet save(Retweet retweet);

    List<Retweet> findAll();

    void deleteByOriginalTweetIdAndUserId(Long originalTweetId, Long userId);

    boolean existsByOriginalTweetIdAndUserId(Long originalTweetId, Long userId);

    List<Retweet> findByUserIds(Set<Long> userIds);

    List<Retweet> findByOriginalTweetId(Long originalTweetId);

    Optional<Retweet> findByOriginalTweetIdAndUserId(Long originalTweetId, Long userId);

}
