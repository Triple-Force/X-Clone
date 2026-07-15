package logic_core.infrastructure.mapper;

import Shared.Models.Follow.Follow;
import Shared.Models.User.User;
import jakarta.persistence.EntityManager;
import logic_core.domain.model.FollowRelation;

public final class FollowPersistenceMapper
{
    private FollowPersistenceMapper()
    {
    }

    public static FollowRelation toDomain(Follow entity)
    {
        if (entity == null)
        {
            return null;
        }

        return FollowRelation.create(
                entity.getFollower().getId(),
                entity.getFollowing().getId(),
                entity.getCreatedAt()
        );
    }

    public static Follow toPersistence(
            FollowRelation relation,
            EntityManager entityManager
    )
    {
        if (relation == null)
        {
            return null;
        }


        User follower = entityManager.getReference(
                User.class,
                relation.getFollowerId()
        );

        User following = entityManager.getReference(
                User.class,
                relation.getFollowingId()
        );

        return Follow.builder()
                .follower(follower)
                .following(following)
                .createdAt(relation.getCreatedAt())
                .build();
    }
}
