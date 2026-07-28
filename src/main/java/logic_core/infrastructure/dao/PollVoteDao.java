package logic_core.infrastructure.dao;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.PollVote.PollVote;

import java.util.Optional;
import java.util.UUID;

public class PollVoteDao extends GenericDAO<PollVote>
{
    public PollVoteDao()
    {
        super(PollVote.class);
    }

    public Optional<PollVote> findUserVote(UUID userId, UUID pollId)
    {
        return Optional.ofNullable(
                findOneByJpql(
                        "SELECT pv FROM PollVote pv WHERE pv.user.id = :userId AND pv.option.poll.id = :pollId",
                        q -> q.setParameter("userId", userId)
                                .setParameter("pollId", pollId)
                )
        );
    }

    public long countVotesByOptionId(UUID optionId)
    {
        return countByJpql(
                "SELECT COUNT(pv) FROM PollVote pv WHERE pv.option.id = :optionId",
                q -> q.setParameter("optionId", optionId)
        );
    }
}
