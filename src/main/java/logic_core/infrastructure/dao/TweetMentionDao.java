package logic_core.infrastructure.dao;

import Shared.Models.TweetMention.TweetMention;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TweetMentionDao extends AbstractJpaDao<TweetMention>
{
    public TweetMentionDao(EntityManager entityManager)
    {
        super(entityManager, TweetMention.class);
    }

    public void insert(TweetMention tweetMention)
    {
        persist(tweetMention);
    }

    public void update(TweetMention tweetMention)
    {
        merge(tweetMention);
    }

    public void delete(TweetMention tweetMention)
    {
        remove(tweetMention);
    }

    public Optional<TweetMention> findRelation(UUID tweetId, UUID mentionedUserId)
    {
        List<TweetMention> results = entityManager.createQuery(
                        "SELECT tm FROM TweetMention tm WHERE tm.tweet.id = :tweetId AND tm.mentionedUser.id = :mentionedUserId",
                        TweetMention.class
                )
                .setParameter("tweetId", tweetId)
                .setParameter("mentionedUserId", mentionedUserId)
                .setMaxResults(1)
                .getResultList();

        return results.stream().findFirst();
    }

    public List<TweetMention> findByTweetId(UUID tweetId)
    {
        return entityManager.createQuery(
                        "SELECT tm FROM TweetMention tm WHERE tm.tweet.id = :tweetId",
                        TweetMention.class
                )
                .setParameter("tweetId", tweetId)
                .getResultList();
    }

    public List<TweetMention> findByMentionedUserId(UUID mentionedUserId)
    {
        return entityManager.createQuery(
                        "SELECT tm FROM TweetMention tm WHERE tm.mentionedUser.id = :mentionedUserId ORDER BY tm.tweet.createdAt DESC",
                        TweetMention.class
                )
                .setParameter("mentionedUserId", mentionedUserId)
                .getResultList();
    }
}
