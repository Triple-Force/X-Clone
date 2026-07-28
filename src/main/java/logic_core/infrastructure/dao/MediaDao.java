package logic_core.infrastructure.dao;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Media.Media;

import java.util.List;
import java.util.UUID;

public class MediaDao extends GenericDAO<Media>
{
    public MediaDao()
    {
        super(Media.class);
    }

    public List<Media> findByTweetId(UUID tweetId)
    {
        return findByJpql(
                "SELECT m FROM Media m WHERE m.tweet.id = :tweetId",
                query -> query.setParameter("tweetId", tweetId)
        );
    }
}
