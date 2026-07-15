package logic_core.infrastructure.dao;

import Shared.Models.TweetEdit.TweetEdit;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.UUID;

public class TweetEditDao extends AbstractJpaDao<TweetEdit>
{
    public TweetEditDao(EntityManager entityManager)
    {
        super(entityManager, TweetEdit.class);
    }

    public List<TweetEdit> findHistoryByTweetId(UUID tweetId)
    {
        return entityManager.createQuery(
                        "SELECT te FROM TweetEdit te WHERE te.tweet.id = :tweetId ORDER BY te.createdAt DESC",
                        TweetEdit.class
                )
                .setParameter("tweetId", tweetId)
                .getResultList();
    }
}
