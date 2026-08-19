package logic_core.infrastructure.dao;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.TweetHashtag.TweetHashtag;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TweetHashtagDao extends GenericDAO<TweetHashtag>
{
    public TweetHashtagDao()
    {
        super(TweetHashtag.class);
    }

    public void insert(TweetHashtag tweetHashtag)
    {
        super.insert(tweetHashtag);
    }

    public void updateTweetHashtag(TweetHashtag tweetHashtag)
    {
        super.update(tweetHashtag);
    }

    public void delete(TweetHashtag tweetHashtag)
    {
        super.delete(tweetHashtag);
    }

    public Optional<TweetHashtag> findRelation(UUID tweetId, UUID hashtagId)
    {
        return Optional.ofNullable(
                findOneByJpql(
                        """
                        SELECT th
                        FROM TweetHashtag th
                        WHERE
                            th.tweet.id = :tweetId
                            AND th.hashtag.id = :hashtagId
                            AND th.tweet.isDeleted = false
                        """,
                        q -> q.setParameter("tweetId", tweetId)
                                .setParameter("hashtagId", hashtagId)
                )
        );
    }

    public List<TweetHashtag> findByTweetId(UUID tweetId)
    {
        return findByJpql(
                """
                SELECT th
                FROM TweetHashtag th
                WHERE
                    th.tweet.id = :tweetId
                    AND th.tweet.isDeleted = false
                """,
                q -> q.setParameter("tweetId", tweetId)
        );
    }

    public List<TweetHashtag> findByHashtagId(UUID hashtagId)
    {
        return findByJpql(
                """
                SELECT th
                FROM TweetHashtag th
                WHERE
                    th.hashtag.id = :hashtagId
                    AND th.tweet.isDeleted = false
                """,
                q -> q.setParameter("hashtagId", hashtagId)
        );
    }
}
