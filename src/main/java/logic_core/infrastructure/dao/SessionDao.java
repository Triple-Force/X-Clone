package logic_core.infrastructure.dao;

import Shared.Models.Session.Session;
import jakarta.persistence.EntityManager;
import logic_core.common.util.TimeProvider;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class SessionDao extends AbstractJpaDao<Session>
{
    private final TimeProvider timeProvider;

    public SessionDao(EntityManager entityManager, TimeProvider timeProvider)
    {
        super(entityManager, Session.class);
        this.timeProvider = timeProvider;
    }

    public Optional<Session> findValidSession(String token, OffsetDateTime now)
    {
        String jpql = """
            select s
            from Session s
            where s.token = :token
              and s.expiresAt > :now
            """;

        return entityManager.createQuery(jpql, Session.class)
                .setParameter("token", token)
                .setParameter("now", now)
                .getResultStream()
                .findFirst();
    }


    public Session replaceUserSession(UUID userId, Session newSession)
    {
            entityManager.createQuery("""
                    delete from Session s
                    where s.user.id = :userId
                    """)
                    .setParameter("userId", userId)
                    .executeUpdate();

            entityManager.persist(newSession);

            return newSession;
    }

    public void insert(Session session)
    {
        Objects.requireNonNull(session, "session must not be null");
        persist(session);
    }

    public void save(Session session)
    {
        Objects.requireNonNull(session, "session must not be null");

        if (session.getId() == null)
        {
            persist(session);
        }
        else
        {
            merge(session);
        }
    }

    public void updateSession(Session updated)
    {
        Objects.requireNonNull(updated, "updated Session must not be null.");
        merge(updated);
    }

    public void delete(Session session)
    {
        Objects.requireNonNull(session, "session must not be null");
        remove(session);
    }

    public Optional<Session> findById(UUID sessionId)
    {
        Objects.requireNonNull(sessionId, "sessionId must not be null.");
        return super.findById(sessionId);
    }

    public Optional<Session> findByToken(String token)
    {
        Objects.requireNonNull(token, "token must not be null");

        List<Session> results = entityManager.createQuery(
                        "SELECT s FROM Session s WHERE s.token = :token",
                        Session.class
                )
                .setParameter("token", token)
                .setMaxResults(1)
                .getResultList();

        return results.stream().findFirst();
    }

    public List<Session> findActiveSessionsByUserId(UUID userId)
    {
        return entityManager.createQuery(
                        "SELECT s FROM Session s WHERE s.user.id = :userId AND s.expiresAt > :now",
                        Session.class
                )
                .setParameter("userId", userId)
                .setParameter("now", timeProvider.now())
                .getResultList();
    }

    public void invalidateByToken(String token)
    {;
            entityManager.createQuery("""
                delete from Session s
                where s.token = :token
                """)
                    .setParameter("token", token)
                    .executeUpdate();
    }


    public List<Session> findAll()
    {
        return entityManager.createQuery("SELECT s FROM Session s", Session.class)
                .getResultList();
    }

}
