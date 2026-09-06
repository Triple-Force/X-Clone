package logic_core.infrastructure.repository;

import logic_core.domain.model.*;
import logic_core.domain.repository.RelationshipRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring-managed implementation of {@link RelationshipRepository}.
 * <p>
 * Delegates each relationship operation to the appropriate feature-specific adapter:
 * <ul>
 *   <li>{@link FollowRepositoryAdapter} for Follow operations</li>
 *   <li>{@link BlockRepositoryAdapter} for Block operations</li>
 *   <li>{@link MuteRepositoryAdapter} for Mute operations</li>
 *   <li>{@link LikeRepositoryAdapter} for Like operations</li>
 * </ul>
 * <p>
 * This adapter replaces the legacy {@code RelationshipJpaRepository} which depended on
 * legacy DAOs and EntityManager. The new adapter has NO dependency on:
 * <ul>
 *   <li>FollowDao, BlockDao, MuteDao, LikeDao</li>
 *   <li>EntityManager</li>
 *   <li>Shared.Models.* legacy entities</li>
 *   <li>Legacy persistence mappers</li>
 * </ul>
 */
@Component
@Transactional
public class RelationshipRepositoryAdapter implements RelationshipRepository {

    private final FollowRepositoryAdapter followRepositoryAdapter;
    private final BlockRepositoryAdapter blockRepositoryAdapter;
    private final MuteRepositoryAdapter muteRepositoryAdapter;
    private final LikeRepositoryAdapter likeRepositoryAdapter;

    public RelationshipRepositoryAdapter(
            FollowRepositoryAdapter followRepositoryAdapter,
            BlockRepositoryAdapter blockRepositoryAdapter,
            MuteRepositoryAdapter muteRepositoryAdapter,
            LikeRepositoryAdapter likeRepositoryAdapter) {
        this.followRepositoryAdapter = followRepositoryAdapter;
        this.blockRepositoryAdapter = blockRepositoryAdapter;
        this.muteRepositoryAdapter = muteRepositoryAdapter;
        this.likeRepositoryAdapter = likeRepositoryAdapter;
    }

    // =====================================================================
    // Follow operations — delegate to FollowRepositoryAdapter
    // =====================================================================

    @Override
    public void saveFollow(FollowRelation follow) {
        followRepositoryAdapter.saveFollow(follow);
    }

    @Override
    public void deleteFollow(FollowRelation follow) {
        followRepositoryAdapter.deleteFollow(follow);
    }

    @Override
    public Optional<FollowRelation> findFollowRelation(UUID followerId, UUID followingId) {
        return followRepositoryAdapter.findFollowRelation(followerId, followingId);
    }

    @Override
    public List<FollowRelation> findByFollowerId(UUID followerId) {
        return followRepositoryAdapter.findByFollowerId(followerId);
    }

    @Override
    public List<FollowRelation> findByFollowingId(UUID followingId) {
        return followRepositoryAdapter.findByFollowingId(followingId);
    }

    @Override
    public boolean isFollowing(UUID followerId, UUID followingId) {
        return followRepositoryAdapter.isFollowing(followerId, followingId);
    }

    @Override
    public boolean existsFollowRelation(UUID userId1, UUID userId2) {
        return followRepositoryAdapter.existsFollowRelation(userId1, userId2);
    }

    @Override
    public long countFollowers(UUID userId) {
        return followRepositoryAdapter.countFollowers(userId);
    }

    @Override
    public long countFollowing(UUID userId) {
        return followRepositoryAdapter.countFollowing(userId);
    }

    /**
     * Returns an empty list — this is a stub implementation.
     * <p>
     * The legacy {@code RelationshipJpaRepository.getFollowers()} also returned
     * {@code List.of()} with a TODO comment. The actual follower retrieval
     * is handled by {@code GetFollowersUseCase} using
     * {@code RelationshipRepository.findByFollowingId()} instead.
     * <p>
     * This stub preserves the existing runtime behavior exactly.
     */
    @Override
    public List<UserModel> getFollowers(UUID userId, int limit, int offset) {
        //TODO — preserving legacy stub behavior
        return List.of();
    }

    // =====================================================================
    // Block operations — delegate to BlockRepositoryAdapter
    // =====================================================================

    @Override
    public void saveBlock(BlockRelation block) {
        blockRepositoryAdapter.saveBlock(block);
    }

    @Override
    public void deleteBlock(BlockRelation block) {
        blockRepositoryAdapter.deleteBlock(block);
    }

    @Override
    public Optional<BlockRelation> findBlockRelation(UUID blockerId, UUID blockedId) {
        return blockRepositoryAdapter.findBlockRelation(blockerId, blockedId);
    }

    @Override
    public List<BlockRelation> findByBlockerId(UUID blockerId) {
        return blockRepositoryAdapter.findByBlockerId(blockerId);
    }

    @Override
    public boolean isBlockedBy(UUID blockerId, UUID blockedId) {
        return blockRepositoryAdapter.isBlockedBy(blockerId, blockedId);
    }

    @Override
    public boolean existsBlockRelation(UUID userA, UUID userB) {
        return blockRepositoryAdapter.existsBlockRelation(userA, userB);
    }

    // =====================================================================
    // Mute operations — delegate to MuteRepositoryAdapter
    // =====================================================================

    @Override
    public void saveMute(MuteRelation mute) {
        muteRepositoryAdapter.saveMute(mute);
    }

    @Override
    public void deleteMute(MuteRelation mute) {
        muteRepositoryAdapter.deleteMute(mute);
    }

    @Override
    public Optional<MuteRelation> findMuteRelation(UUID muterId, UUID mutedId) {
        return muteRepositoryAdapter.findMuteRelation(muterId, mutedId);
    }

    @Override
    public List<MuteRelation> findByMuterId(UUID muterId) {
        return muteRepositoryAdapter.findByMuterId(muterId);
    }

    @Override
    public boolean isMutedBy(UUID muterId, UUID mutedId) {
        return muteRepositoryAdapter.isMutedBy(muterId, mutedId);
    }

    @Override
    public boolean existsMuteRelation(UUID userA, UUID userB) {
        return muteRepositoryAdapter.existsMuteRelation(userA, userB);
    }

    // =====================================================================
    // Like operations — delegate to LikeRepositoryAdapter
    // =====================================================================

    @Override
    public Boolean existsLikeRelation(UUID userID, UUID tweetId) {
        return likeRepositoryAdapter.existsLikeRelation(userID, tweetId);
    }

    @Override
    public void saveLike(LikeRelation like) {
        likeRepositoryAdapter.saveLike(like);
    }

    @Override
    public void deleteLike(LikeRelation like) {
        likeRepositoryAdapter.deleteLike(like);
    }

    @Override
    public List<LikeRelation> findLikesByTweetId(UUID tweetId) {
        return likeRepositoryAdapter.findLikesByTweetId(tweetId);
    }

    @Override
    public boolean hasLiked(UUID userId, UUID tweetId) {
        return likeRepositoryAdapter.hasLiked(userId, tweetId);
    }

    @Override
    public long countLikesByTweetId(UUID tweetId) {
        return likeRepositoryAdapter.countLikesByTweetId(tweetId);
    }

    @Override
    public void deleteLikesByTweetId(UUID tweetId) {
        likeRepositoryAdapter.deleteByTweetId(tweetId);
    }
}
