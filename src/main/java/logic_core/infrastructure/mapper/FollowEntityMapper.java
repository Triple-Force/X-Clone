package logic_core.infrastructure.mapper;

import logic_core.domain.model.FollowRelation;
import logic_core.infrastructure.persistence.entity.follow.FollowEntity;
import logic_core.infrastructure.persistence.entity.UserEntity;

public final class FollowEntityMapper {

    private FollowEntityMapper() {
    }

    /**
     * Maps a FollowEntity to the domain FollowRelation model.
     *
     * Extracts follower/following UUIDs from the entity's UserEntity references
     * and preserves the createdAt timestamp.
     */
    public static FollowRelation toDomain(FollowEntity entity) {
        if (entity == null) {
            return null;
        }

        return FollowRelation.create(
                entity.getFollower().getId(),
                entity.getFollowing().getId(),
                entity.getCreatedAt()
        );
    }

    /**
     * Maps a FollowRelation domain model to a new FollowEntity.
     *
     * The caller must provide resolved UserEntity references for follower and following.
     * Use UserJpaRepository.getReferenceById() to obtain lazy proxies.
     */
    public static FollowEntity toPersistence(
            FollowRelation relation,
            UserEntity follower,
            UserEntity following) {
        if (relation == null) {
            return null;
        }

        FollowEntity entity = new FollowEntity();
        entity.setFollower(follower);
        entity.setFollowing(following);
        entity.setCreatedAt(relation.getCreatedAt());
        return entity;
    }
}
