package logic_core.infrastructure.repository;

import Shared.Models.Session.Session;
import jakarta.persistence.EntityManager;
import logic_core.domain.repository.SessionRepository;
import logic_core.infrastructure.dao.SessionDao;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class JpaSessionRepository implements SessionRepository
{
    private final SessionDao sessionDao;
    private final EntityManager entityManager;

    public JpaSessionRepository(SessionDao sessionDao, EntityManager entityManager)
    {
        this.sessionDao = Objects.requireNonNull(sessionDao, "sessionDao must not be null");
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager must not be null");
    }

    @Override
    public Optional<Session> findById(UUID sessionId)
    {
        return sessionDao.findById(sessionId);
    }

    @Override
    public Optional<Session> findByToken(String token)
    {
        return sessionDao.findByToken(token);
    }

    @Override
    public List<Session> findActiveSessionsByUserId(UUID userId)
    {
        return sessionDao.findActiveSessionsByUserId(userId);
    }

    @Override
    public Optional<Session> findByRefreshToken(String refreshToken)
    {
        return findByToken(refreshToken);
    }

    @Override
    public void save(Session session)
    {
        sessionDao.save(session);
    }

    @Override
    public void update(Session session)
    {
        sessionDao.updateSession(session);
    }

    @Override
    public void revokeById(UUID sessionId)
    {
        Session entity = sessionDao.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("session.not_found"));

        entityManager.remove(entity);
    }

    @Override
    public void revoke(Session session)
    {
        if (session == null || session.getId() == null)
        {
            throw new IllegalArgumentException("session.id_is_required");
        }

        revokeById(session.getId());
    }

    public Optional<OffsetDateTime> findExpireTimeById(UUID sessionId)
    {

        List<OffsetDateTime> results = entityManager.createQuery(
                        "SELECT s.expiresAt FROM Session s WHERE s.id = :sessionId",
                        OffsetDateTime.class
                )
                .setParameter("sessionId", sessionId)
                .setMaxResults(1)
                .getResultList();

        return results.stream().findFirst();
    }
}