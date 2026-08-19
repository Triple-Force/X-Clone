package logic_core.infrastructure.dao;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.TweetEdit.TweetEdit;

import java.util.List;
import java.util.UUID;

public class TweetEditDao extends GenericDAO<TweetEdit>
{
    public TweetEditDao()
    {
        super(TweetEdit.class);
    }

    public TweetEdit save(TweetEdit tweetEdit)
    {
        insert(tweetEdit);
        return tweetEdit;
    }

    public List<TweetEdit> findHistoryByTweetId(UUID tweetId)
    {
        return findByJpql(
                """
                SELECT te
                FROM TweetEdit te
                WHERE
                    te.tweet.id = :tweetId
                    AND te.tweet.isDeleted = false
                ORDER BY te.createdAt DESC
                """,
                q -> q.setParameter("tweetId", tweetId)
        );
    }
}
