package logic_core.infrastructure.dao;

import Shared.Models.TweetHashtag.TweetHashtag;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TweetHashtagDao extends AbstractJpaDao<TweetHashtag>
{
    public TweetHashtagDao(EntityManager entityManager)
    {
        super(entityManager, TweetHashtag.class);
    }

    public void insert(TweetHashtag tweetHashtag)
    {
        persist(tweetHashtag);
    }

    public void update(TweetHashtag tweetHashtag)
    {
        merge(tweetHashtag);
    }

    public void delete(TweetHashtag tweetHashtag)
    {
        remove(tweetHashtag);
    }

    public Optional<TweetHashtag> findRelation(UUID tweetId, UUID hashtagId)
    {
        List<TweetHashtag> results = entityManager.createQuery(
                        "SELECT th FROM TweetHashtag th WHERE th.tweet.id = :tweetId AND th.hashtag.id = :hashtagId",
                        TweetHashtag.class
                )
                .setParameter("tweetId", tweetId)
                .setParameter("hashtagId", hashtagId)
                .setMaxResults(1)
                .getResultList();

        return results.stream().findFirst();
    }

    public List<TweetHashtag> findByTweetId(UUID tweetId)
    {
        return entityManager.createQuery(
                        "SELECT th FROM TweetHashtag th WHERE th.tweet.id = :tweetId",
                        TweetHashtag.class
                )
                .setParameter("tweetId", tweetId)
                .getResultList();
    }

    public List<TweetHashtag> findByHashtagId(UUID hashtagId)
    {
        return entityManager.createQuery(
                        "SELECT th FROM TweetHashtag th WHERE th.hashtag.id = :hashtagId",
                        TweetHashtag.class
                )
                .setParameter("hashtagId", hashtagId)
                .getResultList();
    }
}
