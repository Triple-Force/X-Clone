package logic_core.infrastructure.dao;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.TweetMention.TweetMention;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TweetMentionDao extends GenericDAO<TweetMention>
{
    public TweetMentionDao()
    {
        super(TweetMention.class);
    }

    public void insert(TweetMention tweetMention)
    {
        super.insert(tweetMention);
    }

    public void updateTweetMention(TweetMention tweetMention)
    {
        super.update(tweetMention);
    }

    public void delete(TweetMention tweetMention)
    {
        super.delete(tweetMention);
    }

    public Optional<TweetMention> findRelation(UUID tweetId, UUID mentionedUserId)
    {
        return Optional.ofNullable(
                findOneByJpql(
                        """
                        SELECT tm
                        FROM TweetMention tm
                        WHERE
                            tm.tweet.id = :tweetId
                            AND tm.mentionedUser.id = :mentionedUserId
                            AND tm.tweet.isDeleted = false
                            AND tm.mentionedUser.isDeleted = false
                        """,
                        q -> q.setParameter("tweetId", tweetId)
                                .setParameter("mentionedUserId", mentionedUserId)
                )
        );
    }

    public List<TweetMention> findByTweetId(UUID tweetId)
    {
        return findByJpql(
                """
                SELECT tm
                FROM TweetMention tm
                WHERE
                    tm.tweet.id = :tweetId
                    AND tm.tweet.isDeleted = false
                    AND tm.mentionedUser.isDeleted = false
                """,
                q -> q.setParameter("tweetId", tweetId)
        );
    }

    public List<TweetMention> findByMentionedUserId(UUID mentionedUserId)
    {
        return findByJpql(
                """
                SELECT tm
                FROM TweetMention tm
                WHERE
                    tm.mentionedUser.id = :mentionedUserId
                    AND tm.mentionedUser.isDeleted = false
                    AND tm.tweet.isDeleted = false
                ORDER BY tm.tweet.createdAt DESC
                """,
                q -> q.setParameter("mentionedUserId", mentionedUserId)
        );
    }
}
