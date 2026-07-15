package logic_core.infrastructure.dao;

import Shared.Models.Tweet.Tweet;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TweetDao extends AbstractJpaDao<Tweet>
{
    public TweetDao(EntityManager entityManager)
    {
        super(entityManager, Tweet.class);
    }

    public Boolean isRetweetedByUser(UUID tweetId, UUID userId)
    {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(t) FROM Tweet t WHERE t.retweetedTweet.id = :tweetId AND t.author.id = :userId",
                        Long.class
                )
                .setParameter("tweetId", tweetId)
                .setParameter("userId", userId)
                .getSingleResult();

        return count > 0;
    }

    public Boolean isRepliedByUser(UUID tweetId, UUID userId)
    {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(t) FROM Tweet t WHERE t.repliedToTweet.id = :tweetId AND t.author.id = :userId",
                        Long.class
                )
                .setParameter("tweetId", tweetId)
                .setParameter("userId", userId)
                .getSingleResult();

        return count > 0;
    }

    public List<Tweet> findByAuthorId(UUID authorId)
    {
        return entityManager.createQuery("SELECT t FROM Tweet t WHERE t.author.id = :authorId ORDER BY t.createdAt DESC", Tweet.class)
                .setParameter("authorId", authorId).getResultList();
    }

    public List<Tweet> findRepliesByTweetId(UUID parentTweetId)
    {
        return entityManager.createQuery("SELECT t FROM Tweet t WHERE t.repliedToTweet.id = :parentTweetId ORDER BY t.createdAt ASC", Tweet.class)
                .setParameter("parentTweetId", parentTweetId).getResultList();
    }

    public List<Tweet> findTimelineTweets(UUID userId)
    {
        return entityManager.createQuery(
                        "SELECT t FROM Tweet t WHERE t.author.id = :userId OR t.author.id IN " +
                                "(SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId) ORDER BY t.createdAt DESC", Tweet.class)
                .setParameter("userId", userId).getResultList();
    }

    public List<Tweet> findRetweetsOfTweet(UUID originalTweetId)
    {
        return entityManager.createQuery("SELECT t FROM Tweet t WHERE t.retweetedTweet.id = :originalTweetId ORDER BY t.createdAt DESC", Tweet.class)
                .setParameter("originalTweetId", originalTweetId).getResultList();
    }

    public List<Tweet> findQuotesOfTweet(UUID originalTweetId)
    {
        return entityManager.createQuery("SELECT t FROM Tweet t WHERE t.quotedTweet.id = :originalTweetId ORDER BY t.createdAt DESC", Tweet.class)
                .setParameter("originalTweetId", originalTweetId).getResultList();
    }

    public List<Tweet> findTweetsByHashtag(UUID hashtagId)
    {
        return entityManager.createQuery("SELECT th.tweet FROM TweetHashtag th WHERE th.hashtag.id = :hashtagId ORDER BY th.tweet.createdAt DESC", Tweet.class)
                .setParameter("hashtagId", hashtagId).getResultList();
    }

    public Optional<Tweet> findById(UUID tweetId)
    {
        return super.findById(tweetId);
    }

    public List<Tweet> findTweetsRepliedByUser(UUID userId)
    {
        return entityManager.createQuery(
                        "SELECT DISTINCT t.repliedToTweet FROM Tweet t " +
                                "WHERE t.author.id = :userId " +
                                "AND t.repliedToTweet IS NOT NULL " +
                                "ORDER BY t.repliedToTweet.createdAt DESC", Tweet.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<Tweet> findTweetsRetweetedByUser(UUID userId)
    {
        return entityManager.createQuery(
                        "SELECT DISTINCT t.retweetedTweet FROM Tweet t " +
                                "WHERE t.author.id = :userId " +
                                "AND t.retweetedTweet IS NOT NULL " +
                                "ORDER BY t.retweetedTweet.createdAt DESC", Tweet.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}
