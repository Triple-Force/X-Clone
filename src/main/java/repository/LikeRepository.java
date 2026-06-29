package repository;

import java.util.Set;

public interface LikeRepository
{
    void like(Long userId, Long tweetId);

    void unlike(Long userId, Long tweetId);

    boolean isLikedByUser(Long userId, Long tweetId);

    Set<Long> findLikedUserIdsByTweetId(Long tweetId);

    int countByTweetId(Long tweetId);
}
