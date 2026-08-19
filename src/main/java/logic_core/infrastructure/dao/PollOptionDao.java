package logic_core.infrastructure.dao;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.PollOption.PollOption;

import java.util.List;
import java.util.UUID;

public class PollOptionDao extends GenericDAO<PollOption>
{
    public PollOptionDao()
    {
        super(PollOption.class);
    }

    public List<PollOption> findByPollId(UUID pollId)
    {
        return findByJpql(
                "SELECT po FROM PollOption po WHERE po.poll.id = :pollId ORDER BY po.id ASC",
                q -> q.setParameter("pollId", pollId)
        );
    }
}
