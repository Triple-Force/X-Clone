package repository;


import java.util.Set;

public interface FollowRepository
{
    void follow(Long followerId, Long followingId);

    void unfollow(Long followerId, Long followingId);

    boolean isFollowing(Long followerId, Long followingId);

    Set<Long> findFollowingIdsByUserId(Long userId);

    Set<Long> findFollowerIdsByUserId(Long userId);
}
