package logic_core.session;

import logic_core.common.util.TimeProvider;
import logic_core.domain.model.SessionModel;
import logic_core.domain.repository.SessionRepository;
import logic_core.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionManager
{
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final SessionFactory sessionFactory;
    private final TimeProvider timeProvider;

    @Transactional
    public SessionModel startSession(UUID userId)
    {
        userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        SessionModel newSession = sessionFactory.create(userId);

        return sessionRepository.replaceUserSession(userId, newSession);
    }

    @Transactional
    public void invalidateSession(SessionModel session)
    {
        if (session == null || session.getId() == null)
        {
            throw new IllegalArgumentException("session.id_is_required");
        }

        sessionRepository.revokeById(session.getId());
    }

    @Transactional
    public void invalidateSessionByToken(String token)
    {
        sessionRepository.findByToken(token)
                .ifPresent(s -> sessionRepository.revokeById(s.getId()));
    }

    @Transactional
    public void revokeAllForUser(UUID userId)
    {
        if (userId == null)
        {
            throw new IllegalArgumentException("user.id_is_required");
        }

        sessionRepository.revokeAllByUserId(userId);
    }

    public Optional<SessionModel> findValidSession(String token)
    {
        return sessionRepository.findByToken(token)
            .filter(s -> s.getExpiresAt().isAfter(timeProvider.now()));
    }

    public Optional<SessionModel> findByToken(String token)
    {
        return sessionRepository.findByToken(token);
    }
}
