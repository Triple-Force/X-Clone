package logic_core.infrastructure.dao;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Like.Like;
import Shared.Models.Like.LikeId;

import java.util.List;
import java.util.UUID;

public class LikeDao extends GenericDAO<Like>
{
    public LikeDao()
    {
        super(Like.class);
    }

    public void insert(Like like)
    {
       super.insert(like);
    }

    public void delete(Like like)
    {
        hardDelete(like);
    }

    public boolean findRelation(UUID userId, UUID tweetId)
    {
        return countByJpql(
                """
                SELECT COUNT(l)
                FROM Like l
                WHERE l.user.id = :userId
                  AND l.tweet.id = :tweetId
                  AND l.user.isDeleted = false
                  AND l.tweet.isDeleted = false
                """,
                query -> query
                        .setParameter("userId", userId)
                        .setParameter("tweetId", tweetId)
        ) > 0;
    }

    public List<Like> findByTweetId(UUID tweetId)
    {
        return findByJpql(
                """
                SELECT l
                FROM Like l
                WHERE l.tweet.id = :tweetId
                  AND l.tweet.isDeleted = false
                  AND l.user.isDeleted = false
                """,
                query -> query.setParameter("tweetId", tweetId)
        );
    }

    public long countLikesByTweetId(UUID tweetId)
    {
        return countByJpql(
                """
                SELECT COUNT(l)
                FROM Like l
                WHERE l.tweet.id = :tweetId
                  AND l.tweet.isDeleted = false
                  AND l.user.isDeleted = false
                """,
                query -> query.setParameter("tweetId", tweetId)
        );
    }
}
