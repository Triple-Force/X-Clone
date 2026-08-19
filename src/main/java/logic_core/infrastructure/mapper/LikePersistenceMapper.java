package logic_core.infrastructure.mapper;

import Shared.Models.Like.Like;
import Shared.Models.Tweet.Tweet;
import Shared.Models.User.User;
import jakarta.persistence.EntityManager;
import logic_core.domain.model.LikeRelation;


public final class LikePersistenceMapper
{
    private LikePersistenceMapper()
    {
    }

    public static LikeRelation toDomain(Like entity)
    {
        if (entity == null)
        {
            return null;
        }

        return LikeRelation.create(
                entity.getTweet().getId(),
                entity.getUser().getId()
        );
    }

    public static Like toPersistence(
            LikeRelation relation,
            EntityManager entityManager
    )
    {
        if (relation == null)
        {
            return null;
        }


        return Like.builder()
                .user(entityManager.getReference(User.class, relation.getUserId()))
                .tweet(entityManager.getReference(Tweet.class, relation.getTweetId()))
                .createdAt(relation.getCreatedAt())
                .build();
    }
}
