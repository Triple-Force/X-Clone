package logic_core.infrastructure.dao;

import Shared.Models.Like.Like;
import Shared.Models.Like.LikeId;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class LikeDao extends AbstractJpaDao<Like>
{
    public LikeDao(EntityManager entityManager)
    {
        super(entityManager, Like.class);
    }

    public void insert(Like like)
    {
        Objects.requireNonNull(like, "like entity must not be null");
        persist(like);
    }

    public void delete(Like like)
    {
        Objects.requireNonNull(like, "like entity must not be null");
        remove(like);
    }

    public boolean findRelation(UUID userId, UUID tweetId)
    {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(tweetId, "tweetId must not be null");

        LikeId id = new LikeId(userId, tweetId);
        // استفاده از متد findById موجود در AbstractJpaDao
        return findById(id).isPresent();
    }

    public List<Like> findByTweetId(UUID tweetId)
    {
        Objects.requireNonNull(tweetId, "tweetId must not be null");

        return entityManager.createQuery(
                        "SELECT l FROM Like l WHERE l.tweet.id = :tweetId",
                        Like.class
                )
                .setParameter("tweetId", tweetId)
                .getResultList();
    }

    public long countLikesByTweetId(UUID tweetId)
    {
        Objects.requireNonNull(tweetId, "tweetId must not be null");

        Long count = entityManager.createQuery(
                        "SELECT COUNT(l) FROM Like l WHERE l.tweet.id = :tweetId",
                        Long.class
                )
                .setParameter("tweetId", tweetId)
                .getSingleResult();

        return count != null ? count : 0L;
    }
}
