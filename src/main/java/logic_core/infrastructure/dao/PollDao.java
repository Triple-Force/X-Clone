package logic_core.infrastructure.dao;

import Shared.Models.Poll.Poll;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class PollDao extends AbstractJpaDao<Poll>
{
    public PollDao(EntityManager entityManager)
    {
        super(entityManager, Poll.class);
    }

    public Optional<Poll> findByTweetId(UUID tweetId)
    {
        Objects.requireNonNull(tweetId, "tweetId must not be null");

        List<Poll> results = entityManager.createQuery(
                        "SELECT p FROM Poll p WHERE p.tweet.id = :tweetId",
                        Poll.class
                )
                .setParameter("tweetId", tweetId)
                .setMaxResults(1)
                .getResultList();

        return results.stream().findFirst();
    }
}
