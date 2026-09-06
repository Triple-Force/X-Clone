package logic_core.infrastructure.mapper;

import logic_core.domain.model.LikeRelation;
import logic_core.infrastructure.persistence.entity.like.LikeEntity;
import logic_core.infrastructure.persistence.entity.tweet.TweetEntity;
import logic_core.infrastructure.persistence.entity.UserEntity;

public final class LikeEntityMapper {

    private LikeEntityMapper() {
    }

    /**
     * Maps a LikeEntity to the domain LikeRelation model.
     * 
     * Extracts tweet/user UUIDs from the entity's TweetEntity/UserEntity references
     * and preserves the createdAt timestamp.
     * 
     * Note: The order matches legacy LikePersistenceMapper.toDomain():
     * LikeRelation.create(entity.getTweet().getId(), entity.getUser().getId())
     */
    public static LikeRelation toDomain(LikeEntity entity) {
        if (entity == null) {
            return null;
        }

        return LikeRelation.create(
                entity.getTweet().getId(),
                entity.getUser().getId()
        );
    }

    /**
     * Maps a LikeRelation domain model to a new LikeEntity.
     * 
     * The caller must provide resolved UserEntity and TweetEntity references.
     * Use UserJpaRepository.getReferenceById() and TweetJpaRepository.getReferenceById()
     * to obtain lazy proxies.
     */
    public static LikeEntity toPersistence(
            LikeRelation relation,
            UserEntity user,
            TweetEntity tweet) {
        if (relation == null) {
            return null;
        }

        LikeEntity entity = new LikeEntity();
        entity.setUser(user);
        entity.setTweet(tweet);
        entity.setCreatedAt(relation.getCreatedAt());
        return entity;
    }
}
