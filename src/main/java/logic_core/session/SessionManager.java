package logic_core.session;

import Shared.Models.Session.Session;
import Shared.Models.User.User;
import logic_core.common.util.TimeProvider;
import logic_core.infrastructure.dao.SessionDao;
import logic_core.infrastructure.dao.UserDao;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class SessionManager
{
    private final SessionDao sessionDao;
    private final UserDao userDao;
    private final SessionFactory sessionFactory;
    private final TimeProvider timeProvider;

    public Session startSession(UUID userId)
    {
        User managedUser = userDao.findByIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        Session newSession = sessionFactory.create(managedUser);
        return sessionDao.replaceUserSession(userId, newSession);
    }

    public boolean hasActiveSession(UUID userId)
    {
        List<Session> sessions = sessionDao.findActiveSessionsByUserId(userId);
        return sessions != null && !sessions.isEmpty();
    }

    public void invalidateSession(Session session)
    {
        sessionDao.delete(session);
    }

    public void invalidateSessionByToken(String token)
    {
        sessionDao.invalidateByToken(token);
    }

    public Optional<Session> findValidSession(String token)
    {
        return sessionDao.findValidSession(token, timeProvider.now());
    }

    public Optional<Session> findByToken(String token)
    {
        return sessionDao.findByToken(token);
    }
}
