package logic_core.infrastructure.dao;

import Shared.Models.Media.Media;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MediaDao extends AbstractJpaDao<Media>
{
    public MediaDao(EntityManager entityManager)
    {
        super(entityManager, Media.class);
    }

    public List<Media> findByTweetId(UUID tweetId)
    {
        Objects.requireNonNull(tweetId, "tweetId must not be null");

        return entityManager.createQuery(
                        "SELECT m FROM Media m WHERE m.tweet.id = :tweetId",
                        Media.class
                )
                .setParameter("tweetId", tweetId)
                .getResultList();
    }
}
