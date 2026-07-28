package logic_core.app.security;

import logic_core.common.exception.UnauthorizedException;
import logic_core.domain.model.UserModel;
import logic_core.domain.repository.UserRepository;
import logic_core.session.SessionManager;
import Shared.Models.Session.Session;
import logic_core.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.NonNull;
import jakarta.transaction.Transactional;
import java.util.UUID;

@RequiredArgsConstructor
public class AuthLockOrchestrator
{
    @NonNull private final UserRepository userRepository;
    @NonNull private final SessionManager sessionManager;

    @Transactional
    public SessionUserContext lockAndGetContextByToken(String sessionToken)
    {
        Session bootstrap = sessionManager.findByToken(sessionToken)
                .orElseThrow(() -> new NotFoundException("Session not found."));

        UUID userId = resolveUserId(bootstrap);

        UserModel userHint = userRepository.findUserBySessionId(bootstrap.getId())
                .orElseThrow(() -> new NotFoundException("User not found for session."));


        UserModel lockedUser = userRepository.findByIdForUpdate(userHint.getId())
                .orElseThrow(() -> new NotFoundException("User not found during locking."));


        Session session = sessionManager.findValidSession(sessionToken)
                .orElseThrow(() -> new UnauthorizedException("Session invalid or expired."));

        UUID sessionUserId = session.getUser() != null
                ? session.getUser().getId()
                : userRepository.findUserBySessionId(session.getId())
                .orElseThrow(() -> new NotFoundException("User not found for session."))
                .getId();

        if (!lockedUser.getId().equals(sessionUserId))
        {
            throw new UnauthorizedException("Session does not belong to locked user.");
        }

        return new SessionUserContext(lockedUser, session);
    }


    private UUID resolveUserId(Session bootstrap)
    {
        if (bootstrap.getUser() != null && bootstrap.getUser().getId() != null)
        {
            return bootstrap.getUser().getId();
        }
        throw new NotFoundException("Session has no user.");
    }

    @Transactional
    public SessionUserContext lockAndGetContextByUserId(UUID userId)
    {
        UserModel lockedUser = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new NotFoundException("User not found during locking."));

        return new SessionUserContext(lockedUser, null);
    }

    @Transactional
    public SessionUserContext lockAndGetUserByUsername(String username)
    {
        UserModel lockedUser = userRepository.findByUsernameForUpdate(username)
                .orElseThrow(() -> new NotFoundException("User not found during locking."));


        return new SessionUserContext(lockedUser, null);
    }
}
