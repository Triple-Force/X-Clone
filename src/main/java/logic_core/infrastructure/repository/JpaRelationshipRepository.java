package logic_core.infrastructure.repository;

import Shared.Models.Block.Block;
import Shared.Models.Follow.Follow;
import Shared.Models.Like.Like;
import Shared.Models.Mute.Mute;
import jakarta.persistence.EntityManager;
import logic_core.domain.model.*;
import logic_core.domain.repository.RelationshipRepository;
import logic_core.infrastructure.dao.BlockDao;
import logic_core.infrastructure.dao.FollowDao;
import logic_core.infrastructure.dao.LikeDao;
import logic_core.infrastructure.dao.MuteDao;
import logic_core.infrastructure.mapper.BlockPersistenceMapper;
import logic_core.infrastructure.mapper.FollowPersistenceMapper;
import logic_core.infrastructure.mapper.LikePersistenceMapper;
import logic_core.infrastructure.mapper.MutePersistenceMapper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class JpaRelationshipRepository implements RelationshipRepository
{
    private final FollowDao followDao;
    private final BlockDao blockDao;
    private final MuteDao muteDao;
    private final LikeDao likeDao;
    private final EntityManager entityManager;

    public JpaRelationshipRepository(
            FollowDao followDao,
            BlockDao blockDao,
            MuteDao muteDao,
            LikeDao likeDao,
            EntityManager entityManager
    )
    {
        this.followDao = Objects.requireNonNull(followDao, "followDao must not be null");
        this.blockDao = Objects.requireNonNull(blockDao, "blockDao must not be null");
        this.muteDao = Objects.requireNonNull(muteDao, "muteDa must not be null");
        this.likeDao = Objects.requireNonNull(likeDao, "likeDao must not null");
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager must nut be null");
    }

    @Override
    public void saveFollow(FollowRelation follow)
    {
        Follow entity = FollowPersistenceMapper.toPersistence(follow, entityManager);
        followDao.insert(entity);
    }

    @Override
    public void deleteFollow(FollowRelation follow)
    {
        Follow entity = FollowPersistenceMapper.toPersistence(follow, entityManager);
        followDao.delete(entity);
    }

    @Override
    public Optional<FollowRelation> findFollowRelation(UUID followerId, UUID followingId)
    {
        return followDao.findRelation(followerId, followingId)
                .map(FollowPersistenceMapper::toDomain);
    }

    @Override
    public List<FollowRelation> findByFollowerId(UUID followerId)
    {
        return followDao.findByFollowerId(followerId)
                .stream()
                .map(FollowPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<FollowRelation> findByFollowingId(UUID followingId)
    {
        return followDao.findByFollowingId(followingId)
                .stream()
                .map(FollowPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean isFollowing(UUID followerId, UUID followingId)
    {
        return followDao.findRelation(followerId, followingId).isPresent();
    }

    @Override
    public boolean existsFollowRelation(UUID userId1, UUID userId2)
    {
        return isFollowing(userId1, userId2) || isFollowing(userId2, userId1);
    }

    @Override
    public long countFollowers(UUID userId)
    {
        return followDao.countFollowers(userId);
    }

    @Override
    public long countFollowing(UUID userId)
    {
        return followDao.countFollowing(userId);
    }

    @Override
    public List<UserModel> getFollowers(UUID userId, int limit, int offset)
    {
        //TODO
        return List.of();
    }

    @Override
    public void saveBlock(BlockRelation block)
    {
        Block entity = BlockPersistenceMapper.toPersistence(block, entityManager);
        blockDao.insert(entity);
    }

    @Override
    public void deleteBlock(BlockRelation block)
    {
        Block entity = BlockPersistenceMapper.toPersistence(block, entityManager);
        blockDao.delete(entity);
    }

    @Override
    public Optional<BlockRelation> findBlockRelation(UUID blockerId, UUID blockedId)
    {
        return blockDao.findRelation(blockerId, blockedId)
                .map(BlockPersistenceMapper::toDomain);
    }

    @Override
    public List<BlockRelation> findByBlockerId(UUID blockerId)
    {
        return blockDao.findByBlockerId(blockerId)
                .stream()
                .map(BlockPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean isBlockedBy(UUID blockerId, UUID blockedId)
    {
        return blockDao.findRelation(blockerId, blockedId).isPresent();
    }

    @Override
    public boolean existsBlockRelation(UUID userA, UUID userB)
    {
        return blockDao.existsBlockRelation(userA, userB);
    }

    @Override
    public void saveMute(MuteRelation mute)
    {
        Mute entity = MutePersistenceMapper.toPersistence(mute, entityManager);
        muteDao.insert(entity);
    }

    @Override
    public void deleteMute(MuteRelation mute)
    {
        Mute entity = MutePersistenceMapper.toPersistence(mute, entityManager);
        muteDao.delete(entity);
    }

    @Override
    public Optional<MuteRelation> findMuteRelation(UUID muterId, UUID mutedId)
    {
        return muteDao.findRelation(muterId, mutedId)
                .map(MutePersistenceMapper::toDomain);
    }

    @Override
    public List<MuteRelation> findByMuterId(UUID muterId)
    {
        return muteDao.findByMuterId(muterId)
                .stream()
                .map(MutePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean isMutedBy(UUID muterId, UUID mutedId)
    {
        return muteDao.findRelation(muterId, mutedId).isPresent();
    }

    public boolean existsMuteRelation(UUID userA, UUID userB)
    {
        return isMutedBy(userA, userB) || isMutedBy(userB, userA);
    }

    public Boolean existsLikeRelation(UUID userID, UUID tweetId)
    {
        return likeDao.findRelation(userID, tweetId);
    }

    @Override
    public void saveLike(LikeRelation like)
    {
        Like entity = LikePersistenceMapper.toPersistence(like,entityManager);
        likeDao.insert(entity);
    }

    @Override
    public void deleteLike(LikeRelation like)
    {
        Like entity = LikePersistenceMapper.toPersistence(like, entityManager);
        likeDao.delete(entity);
    }


    @Override
    public List<LikeRelation> findLikesByTweetId(UUID tweetId)
    {
        return likeDao.findByTweetId(tweetId)
                .stream()
                .map(LikePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean hasLiked(UUID userId, UUID tweetId)
    {
        return likeDao.findRelation(userId, tweetId);
    }

    @Override
    public long countLikesByTweetId(UUID tweetId)
    {
        return likeDao.countLikesByTweetId(tweetId);
    }

}
