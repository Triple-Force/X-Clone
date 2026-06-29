package repository.mock;


import repository.FollowRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MockFollowRepository implements FollowRepository
{
    // key: followerId, value: set of following user ids
    private final Map<Long, Set<Long>> followingsByUserId = new HashMap<>();

    @Override
    public void follow(Long followerId, Long followingId)
    {
        followingsByUserId
                .computeIfAbsent(followerId, key -> new HashSet<>())
                .add(followingId);
    }

    @Override
    public void unfollow(Long followerId, Long followingId)
    {
        Set<Long> followings = followingsByUserId.get(followerId);
        if (followings != null)
        {
            followings.remove(followingId);

            if (followings.isEmpty())
            {
                followingsByUserId.remove(followerId);
            }
        }
    }

    @Override
    public boolean isFollowing(Long followerId, Long followingId)
    {
        Set<Long> followings = followingsByUserId.get(followerId);
        return followings != null && followings.contains(followingId);
    }

    @Override
    public Set<Long> findFollowingIdsByUserId(Long userId)
    {
        Set<Long> followings = followingsByUserId.get(userId);
        return followings == null ? new HashSet<>() : new HashSet<>(followings);
    }

    @Override
    public Set<Long> findFollowerIdsByUserId(Long userId)
    {
        Set<Long> followerIds = new HashSet<>();

        for (Map.Entry<Long, Set<Long>> entry : followingsByUserId.entrySet())
        {
            Long followerId = entry.getKey();
            Set<Long> followingIds = entry.getValue();

            if (followingIds.contains(userId))
            {
                followerIds.add(followerId);
            }
        }

        return followerIds;
    }
}
