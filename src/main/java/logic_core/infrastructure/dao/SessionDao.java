package logic_core.infrastructure.dao;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Session.Session;
import logic_core.common.util.TimeProvider;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SessionDao extends GenericDAO<Session>
{
    private final TimeProvider timeProvider;

    public SessionDao(TimeProvider timeProvider)
    {
        super(Session.class);
        this.timeProvider = timeProvider;
    }

    public Optional<Session> findValidSession(String token, OffsetDateTime now)
    {
        return Optional.ofNullable(
                findOneByJpql(
                        "SELECT s FROM Session s WHERE s.token = :token AND s.expiresAt > :now",
                        q -> q.setParameter("token", token)
                                .setParameter("now", now)
                )
        );
    }


    public Session replaceUserSession(UUID userId, Session newSession)
    {
        revokeAllByUserId(userId);
        insert(newSession);
        return newSession;
    }

    public void insert(Session session)
    {
        super.insert(session);
    }

    public void save(Session session)
    {
        if (session.getId() == null)
        {
            insert(session);
        }
        else
        {
            upsert(session);
        }
    }

    public void updateSession(Session updated)
    {
        update(updated);
    }

    public void delete(Session session)
    {
       super.delete(session);
    }

    public Optional<Session> findById(UUID sessionId)
    {
        return Optional.ofNullable(super.findById(sessionId));
    }

    public Optional<Session> findByToken(String token)
    {
        return Optional.ofNullable(
                findOneByJpql(
                        "SELECT s FROM Session s WHERE s.token = :token",
                        q -> q.setParameter("token", token)
                )
        );
    }

    public List<Session> findActiveSessionsByUserId(UUID userId)
    {
        return findByJpql(
                "SELECT s FROM Session s WHERE s.user.id = :userId AND s.expiresAt > :now",
                q -> q.setParameter("userId", userId)
                        .setParameter("now", timeProvider.now())
        );
    }

    public void invalidateByToken(String token)
    {
        findByJpql(
                "SELECT s FROM Session s WHERE s.token = :token",
                q -> q.setParameter("token", token)
        ).forEach(this::hardDelete);
    }

    public List<Session> findAll()
    {
        return super.findAll();
    }

    public void revokeAllByUserId(UUID userId)
    {
        findByJpql(
                "SELECT s FROM Session s WHERE s.user.id = :userId",
                q -> q.setParameter("userId", userId)
        ).forEach(this::hardDelete);
    }
}
