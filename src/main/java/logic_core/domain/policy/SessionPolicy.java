package logic_core.domain.policy;

import Shared.Models.Session.Session;
import logic_core.common.exception.NotFoundException;
import logic_core.domain.model.UserModel;
import logic_core.domain.repository.SessionRepository;
import logic_core.domain.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public class SessionPolicy
{
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public SessionPolicy(SessionRepository sessionRepository, UserRepository userRepository)
    {
        this.sessionRepository = Objects.requireNonNull(sessionRepository, "sessionRepository must not be null");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
    }

    public void validateRefresh(Session oldSession, OffsetDateTime now)
    {
        Objects.requireNonNull(oldSession, "oldSession must not be null");
        Objects.requireNonNull(now, "now must not be null");

        UUID sessionId = Objects.requireNonNull(
                oldSession.getId(),
                "session.id_is_required"
        );

        Session persistedSession = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("session.not_found"));

        OffsetDateTime expiresAt = persistedSession.getExpiresAt();

        if (expiresAt == null)
        {
            throw new IllegalStateException("session.expiration_time_not_set");
        }

        if (!expiresAt.isAfter(now))
        {
            throw new IllegalStateException("session.expired");
        }

        UserModel user = userRepository.findUserBySessionId(sessionId)
                .orElseThrow(() -> new NotFoundException(
                        "user.not_found_for_session"
                ));

        validateUserCanRefresh(user);
    }


    private void validateUserCanRefresh(UserModel user)
    {
        if (user == null)
        {
            throw new IllegalStateException("user.not_found");
        }

        if (user.getId() == null)
        {
            throw new IllegalStateException("user.id_is_required");
        }


        if (!userRepository.isActive(user))
        {
            throw new IllegalStateException("user.inActive");
        }

    }
}
