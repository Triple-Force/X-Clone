package logic_core.infrastructure.dao;

import Shared.Models.PollOption.PollOption;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PollOptionDao extends AbstractJpaDao<PollOption>
{
    public PollOptionDao(EntityManager entityManager)
    {
        super(entityManager, PollOption.class);
    }

    public List<PollOption> findByPollId(UUID pollId)
    {
        Objects.requireNonNull(pollId, "pollId must not be null");

        return entityManager.createQuery(
                        "SELECT po FROM PollOption po WHERE po.poll.id = :pollId ORDER BY po.id ASC",
                        PollOption.class
                )
                .setParameter("pollId", pollId)
                .getResultList();
    }
}
