package logic_core.domain.repository;

import logic_core.domain.model.BlockRelation;
import logic_core.domain.model.FollowRelation;
import logic_core.domain.model.LikeRelation;
import logic_core.domain.model.MuteRelation;

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

    // Like
    Boolean existsLikeRelation(UUID userID, UUID tweetId);
    void saveLike(LikeRelation like);
    void deleteLike(LikeRelation like);
    List<LikeRelation> findLikesByTweetId(UUID tweetId);
    boolean hasLiked(UUID userId, UUID tweetId);
    long countLikesByTweetId(UUID tweetId);
}
