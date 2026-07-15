package repository.mock;

import repository.LikeRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MockLikeRepository implements LikeRepository
{
    // key: tweetId, value: set of user ids who liked the tweet
    private final Map<Long, Set<Long>> likedUserIdsByTweetId = new HashMap<>();

    @Override
    public void like(Long userId, Long tweetId)
    {
        likedUserIdsByTweetId
                .computeIfAbsent(tweetId, key -> new HashSet<>())
                .add(userId);
    }

    @Override
    public void unlike(Long userId, Long tweetId)
    {
        Set<Long> likedUserIds = likedUserIdsByTweetId.get(tweetId);
        if (likedUserIds != null)
        {
            likedUserIds.remove(userId);

            if (likedUserIds.isEmpty())
            {
                likedUserIdsByTweetId.remove(tweetId);
            }
        }
    }

    @Override
    public boolean isLikedByUser(Long userId, Long tweetId)
    {
        Set<Long> likedUserIds = likedUserIdsByTweetId.get(tweetId);
        return likedUserIds != null && likedUserIds.contains(userId);
    }

    @Override
    public Set<Long> findLikedUserIdsByTweetId(Long tweetId)
    {
        Set<Long> likedUserIds = likedUserIdsByTweetId.get(tweetId);
        return likedUserIds == null ? new HashSet<>() : new HashSet<>(likedUserIds);
    }

    @Override
    public int countByTweetId(Long tweetId)
    {
        Set<Long> likedUserIds = likedUserIdsByTweetId.get(tweetId);
        return likedUserIds == null ? 0 : likedUserIds.size();
    }
}
