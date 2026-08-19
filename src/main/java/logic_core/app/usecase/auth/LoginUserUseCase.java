package logic_core.app.usecase.auth;

import Shared.Models.Session.Session;
import jakarta.transaction.Transactional;
import logic_core.app.dto.request.LoginRequest;
import logic_core.app.dto.response.AuthResponse;
import logic_core.app.dto.validator.LoginValidator;
import logic_core.app.mapper.AuthMapper;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.SessionUserContext;
import logic_core.common.result.Result;
import logic_core.common.security.PasswordHasher;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.event.authentication.UserLoggedInEvent;
import logic_core.domain.model.UserModel;
import logic_core.domain.repository.UserRepository;
import logic_core.session.SessionManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoginUserUseCase
{
    @NonNull private final LoginValidator validator;
    @NonNull private final PasswordHasher passwordHasher;
    @NonNull private final SessionManager sessionManager;
    @NonNull private final EventPublisher eventPublisher;
    @NonNull private final TimeProvider timeProvider;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<AuthResponse> execute(LoginRequest request)
    {
        try
        {
            validator.validate(request.username(), request.password());
        }
        catch (IllegalArgumentException e)
        {
            return Result.failure(e.getMessage());
        }

        SessionUserContext context = lockOrchestrator.lockAndGetUserByUsername(request.username());
        UserModel lockedUser = context.lockedUser();

        boolean passwordMatches = passwordHasher.verify(
                request.password(),
                lockedUser.getPasswordHash()
        );

        if (!passwordMatches)
        {
            return Result.failure("Invalid credentials.");
        }

        Session session = sessionManager.startSession(lockedUser.getId());

        eventPublisher.publish(new UserLoggedInEvent(
                lockedUser.getId(),
                lockedUser.getUsername(),
                session.getId(),
                timeProvider.now()
        ));

        return Result.success(AuthMapper.toResponse(lockedUser, session));
    }
}
