package logic_core.infrastructure.dao;

import Shared.Models.Mute.Mute;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class MuteDao extends AbstractJpaDao<Mute>
{
    public MuteDao(EntityManager entityManager)
    {
        super(entityManager, Mute.class);
    }

    public void insert(Mute mute)
    {
        Objects.requireNonNull(mute, "mute entity must not be null");
        persist(mute);
    }

    public void delete(Mute mute)
    {
        Objects.requireNonNull(mute, "mute entity must not be null");
        remove(mute);
    }

    public Optional<Mute> findRelation(UUID muterId, UUID mutedId)
    {
        Objects.requireNonNull(muterId, "muterId must not be null");
        Objects.requireNonNull(mutedId, "mutedId must not be null");

        List<Mute> results = entityManager.createQuery(
                        "SELECT m FROM Mute m WHERE m.muter.id = :muterId AND m.muted.id = :mutedId",
                        Mute.class
                )
                .setParameter("muterId", muterId)
                .setParameter("mutedId", mutedId)
                .setMaxResults(1)
                .getResultList();

        return results.stream().findFirst();
    }

    public List<Mute> findByMuterId(UUID muterId)
    {
        Objects.requireNonNull(muterId, "muterId must not be null");

        return entityManager.createQuery(
                        "SELECT m FROM Mute m WHERE m.muter.id = :muterId",
                        Mute.class
                )
                .setParameter("muterId", muterId)
                .getResultList();
    }
}
