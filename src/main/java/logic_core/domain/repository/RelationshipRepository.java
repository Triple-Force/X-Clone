package logic_core.domain.repository;

import logic_core.domain.model.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RelationshipRepository
{
    // Follow
    void saveFollow(FollowRelation follow);
    void deleteFollow(FollowRelation follow);
    Optional<FollowRelation> findFollowRelation(UUID followerId, UUID followingId);
    List<FollowRelation> findByFollowerId(UUID followerId);
    List<FollowRelation> findByFollowingId(UUID followingId);
    boolean isFollowing(UUID followerId, UUID followingId);
    boolean existsFollowRelation(UUID userId1, UUID userId2);
    long countFollowers(UUID userId);
    long countFollowing(UUID userId);
    List<UserModel> getFollowers(UUID userId, int limit, int offset);

    // Block
    void saveBlock(BlockRelation block);
    void deleteBlock(BlockRelation block);
    Optional<BlockRelation> findBlockRelation(UUID blockerId, UUID blockedId);
    List<BlockRelation> findByBlockerId(UUID blockerId);
    boolean isBlockedBy(UUID blockerId, UUID blockedId);
    boolean existsBlockRelation(UUID userA, UUID userB);

    // Mute
    void saveMute(MuteRelation mute);
    void deleteMute(MuteRelation mute);
    Optional<MuteRelation> findMuteRelation(UUID muterId, UUID mutedId);
    List<MuteRelation> findByMuterId(UUID muterId);
    boolean isMutedBy(UUID muterId, UUID mutedId);
    boolean existsMuteRelation(UUID userA, UUID userB);

    // Like
    Boolean existsLikeRelation(UUID userID, UUID tweetId);
    void saveLike(LikeRelation like);
    void deleteLike(LikeRelation like);
    List<LikeRelation> findLikesByTweetId(UUID tweetId);
    boolean hasLiked(UUID userId, UUID tweetId);
    long countLikesByTweetId(UUID tweetId);

    // Cascade
    /**
     * Hard-deletes all like records belonging to a tweet.
     * Used during tweet deletion to cascade-delete related likes.
     */
    void deleteLikesByTweetId(UUID tweetId);
}
