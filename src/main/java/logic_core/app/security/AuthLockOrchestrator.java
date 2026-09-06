package logic_core.app.security;

import logic_core.common.exception.UnauthorizedException;
import logic_core.domain.model.UserModel;
import logic_core.domain.repository.UserRepository;
import logic_core.session.SessionManager;
import logic_core.domain.model.SessionModel;
import logic_core.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthLockOrchestrator
{
    @NonNull private final UserRepository userRepository;
    @NonNull private final SessionManager sessionManager;

    @Transactional
    public SessionUserContext lockAndGetContextByToken(String sessionToken)
    {
        SessionModel session = sessionManager.findValidSession(sessionToken)
                .orElseThrow(() -> new NotFoundException("Session not found or invalid."));

        UUID userId = session.getUserId();

        UserModel lockedUser = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new NotFoundException("User not found during locking."));

        if (!lockedUser.getId().equals(session.getUserId()))
        {
            throw new UnauthorizedException("Session does not belong to locked user.");
        }

        return new SessionUserContext(lockedUser, session);
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
