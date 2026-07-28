package logic_core.infrastructure.dao;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Poll.Poll;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class PollDao extends GenericDAO<Poll>
{
    public PollDao()
    {
        super(Poll.class);
    }

    public Optional<Poll> findByTweetId(UUID tweetId)
    {
        return Optional.ofNullable(
                findOneByJpql(
                        """
                        SELECT p
                        FROM Poll p
                        WHERE p.tweet.id = :tweetId
                          AND p.tweet.isDeleted = false
                        """,
                        q -> q.setParameter("tweetId", tweetId)
                )
        );
    }
}
