package logic_core.infrastructure.dao;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Mute.Mute;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MuteDao extends GenericDAO<Mute>
{
    public MuteDao()
    {
        super(Mute.class);
    }

    public void insert(Mute mute)
    {
        super.insert(mute);
    }

    public void delete(Mute mute)
    {
        super.delete(mute);
    }

    public Optional<Mute> findRelation(UUID muterId, UUID mutedId)
    {
        return Optional.ofNullable(
                findOneByJpql(
                        """
                        SELECT m
                        FROM Mute m
                        WHERE m.muter.id = :muterId
                          AND m.muted.id = :mutedId
                          AND m.muter.isDeleted = false
                          AND m.muted.isDeleted = false
                        """,
                        q -> q.setParameter("muterId", muterId)
                                .setParameter("mutedId", mutedId)
                )
        );
    }

    public List<Mute> findByMuterId(UUID muterId)
    {
        return findByJpql(
                """
                SELECT m
                FROM Mute m
                WHERE m.muter.id = :muterId
                  AND m.muter.isDeleted = false
                  AND m.muted.isDeleted = false
                """,
                q -> q.setParameter("muterId", muterId)
        );
    }
}
