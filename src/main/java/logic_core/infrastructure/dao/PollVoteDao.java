package logic_core.infrastructure.dao;

import Shared.Models.PollVote.PollVote;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class PollVoteDao extends AbstractJpaDao<PollVote>
{
    public PollVoteDao(EntityManager entityManager)
    {
        super(entityManager, PollVote.class);
    }

    public Optional<PollVote> findUserVote(UUID userId, UUID pollId)
    {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(pollId, "pollId must not be null");

        List<PollVote> results = entityManager.createQuery(
                        "SELECT pv FROM PollVote pv WHERE pv.user.id = :userId AND pv.option.poll.id = :pollId",
                        PollVote.class
                )
                .setParameter("userId", userId)
                .setParameter("pollId", pollId)
                .setMaxResults(1)
                .getResultList();

        return results.stream().findFirst();
    }

    public long countVotesByOptionId(UUID optionId)
    {
        Objects.requireNonNull(optionId, "optionId must not be null");

        Long count = entityManager.createQuery(
                        "SELECT COUNT(pv) FROM PollVote pv WHERE pv.option.id = :optionId",
                        Long.class
                )
                .setParameter("optionId", optionId)
                .getSingleResult();

        return count != null ? count : 0L;
    }
}
