package Client.cache;

import Client.ClientDAOManager;
import Shared.Models.Tweet.Tweet;
import logic_core.app.dto.response.TweetResponse;

import java.util.List;
import java.util.UUID;

public class TweetCacheService extends BaseCacheService<Tweet>
{
    public TweetCacheService()
    {
        super(ClientDAOManager.getInstance().getTweetDAO());
    }

    public Tweet find(UUID id)
    {
        return dao.findById(id);
    }

    public TweetResponse getTweet(UUID tweetId)
    {
        return dao.findProjectionByJpql(
                """
                SELECT new logic_core.app.dto.response.TweetResponse(
                    t.id,
                    a.id,
                    a.username,
                    a.displayName,
                    a.avatarUrl,
                    t.content,
                    COUNT(DISTINCT l),
                    COUNT(DISTINCT r),
                    COUNT(DISTINCT rt),
                    t.publishedAt
                )
                FROM Tweet t
                JOIN t.author a
                LEFT JOIN t.likes l
                LEFT JOIN t.replies r
                LEFT JOIN t.retweets rt
                WHERE t.id=:id
                  AND t.isDeleted=false
                GROUP BY
                    t.id,
                    a.id,
                    a.username,
                    a.displayName,
                    a.avatarUrl,
                    t.content,
                    t.publishedAt
                """,
                TweetResponse.class,
                q->q.setParameter("id",tweetId)
        ).stream().findFirst().orElse(null);
    }

    public List<TweetResponse> getTweetsByAuthor(UUID authorId)
    {
        return dao.findProjectionByJpql(
                """
                SELECT new logic_core.app.dto.response.TweetResponse(
                    t.id,
                    a.id,
                    a.username,
                    a.displayName,
                    a.avatarUrl,
                    t.content,
                    COUNT(DISTINCT l),
                    COUNT(DISTINCT r),
                    COUNT(DISTINCT rt),
                    t.publishedAt
                )
                FROM Tweet t
                JOIN t.author a
                LEFT JOIN t.likes l
                LEFT JOIN t.replies r
                LEFT JOIN t.retweets rt
                WHERE a.id=:authorId
                  AND t.isDeleted=false
                GROUP BY
                    t.id,
                    a.id,
                    a.username,
                    a.displayName,
                    a.avatarUrl,
                    t.content,
                    t.publishedAt
                ORDER BY t.publishedAt DESC
                """,
                TweetResponse.class,
                q->q.setParameter("authorId",authorId)
        );
    }

    public void cacheTweet(TweetResponse response)
    {
        Tweet tweet = dao.findOneByJpql(
                """
                SELECT t
                FROM Tweet t
                WHERE t.id=:id
                """,
                q->q.setParameter("id",response.id())
        );

        if(tweet==null)
            return;

        tweet.setContent(response.content());

        dao.update(tweet);
    }

    public void cacheTweets(List<TweetResponse> tweets)
    {
        tweets.forEach(this::cacheTweet);
    }

    public void updateTweet(TweetResponse response)
    {
        cacheTweet(response);
    }

    public void deleteTweet(UUID tweetId)
    {
        dao.deleteById(tweetId);
    }

    public void clear()
    {
        dao.findAll().forEach(dao::delete);
    }
}