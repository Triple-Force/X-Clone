package logic_core.infrastructure.dao;

import Shared.Models.HashtagFollow.HashtagFollow;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class HashtagFollowDao extends AbstractJpaDao<HashtagFollow>
{
    public HashtagFollowDao(EntityManager entityManager)
    {
        super(entityManager, HashtagFollow.class);
    }

    public Optional<HashtagFollow> findRelation(UUID userId, UUID hashtagId)
    {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(hashtagId, "hashtagId must not be null");

        List<HashtagFollow> results = entityManager.createQuery(
                        "SELECT hf FROM HashtagFollow hf " +
                                "WHERE hf.user.id = :userId AND hf.hashtag.id = :hashtagId",
                        HashtagFollow.class
                )
                .setParameter("userId", userId)
                .setParameter("hashtagId", hashtagId)
                .setMaxResults(1)
                .getResultList();

        return results.stream().findFirst();
    }

    public List<HashtagFollow> findByUserId(UUID userId)
    {
        Objects.requireNonNull(userId, "userId must not be null");

        return entityManager.createQuery(
                        "SELECT hf FROM HashtagFollow hf WHERE hf.user.id = :userId",
                        HashtagFollow.class
                )
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<HashtagFollow> findByHashtagId(UUID hashtagId)
    {
        Objects.requireNonNull(hashtagId, "hashtagId must not be null");

        return entityManager.createQuery(
                        "SELECT hf FROM HashtagFollow hf WHERE hf.hashtag.id = :hashtagId",
                        HashtagFollow.class
                )
                .setParameter("hashtagId", hashtagId)
                .getResultList();
    }
}
