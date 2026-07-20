package logic_core.session;

import Shared.Models.Session.Session;
import Shared.Models.User.User;
import logic_core.common.util.TimeProvider;
import logic_core.domain.repository.SessionRepository;
import logic_core.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class SessionManager
{
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final SessionFactory sessionFactory;
    private final TimeProvider timeProvider;

    public Session startSession(UUID userId)
    {
        User managedUser = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        Session newSession = sessionFactory.create(managedUser);
        return sessionRepository.replaceUserSession(userId, newSession);
    }

    public boolean hasActiveSession(UUID userId)
    {
        return !sessionRepository.findActiveSessionsByUserId(userId).isEmpty();
    }

    public void invalidateSession(Session session)
    {
        if (session == null || session.getId() == null)
        {
            throw new IllegalArgumentException("session.id_is_required");
        }

        sessionRepository.revoke(session);
    }

    public void invalidateSessionByToken(String token)
    {
        sessionRepository.findByToken(token)
                .ifPresent(sessionRepository::revoke);
    }

    public void revokeAllForUser(UUID userId)
    {
        if (userId == null)
        {
            throw new IllegalArgumentException("user.id_is_required");
        }

        sessionRepository.revokeAllByUserId(userId);
    }

    public Optional<Session> findValidSession(String token)
    {
        return sessionRepository.findByToken(token);}

    public Optional<Session> findByToken(String token)
    {
        return sessionRepository.findByToken(token);
    }
}
