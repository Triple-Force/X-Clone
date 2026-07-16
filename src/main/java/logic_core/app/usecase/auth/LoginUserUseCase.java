package logic_core.app.usecase.auth;

import Shared.Models.Session.Session;
import logic_core.app.dto.request.LoginRequest;
import logic_core.app.dto.response.AuthResponse;
import logic_core.app.dto.validator.LoginValidator;
import logic_core.app.mapper.AuthMapper;
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
    @NonNull private final UserRepository userRepository;
    @NonNull private final LoginValidator validator;
    @NonNull private final PasswordHasher passwordHasher;
    @NonNull private final SessionManager sessionManager;
    @NonNull private final EventPublisher eventPublisher;
    @NonNull private final TimeProvider timeProvider;

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

        return userRepository.findByUsername(request.username())
                .map(user -> authenticateUser(request, user))
                .orElseGet(() -> Result.failure("Invalid credentials."));
    }

    private Result<AuthResponse> authenticateUser(LoginRequest request, UserModel user)
    {
        boolean passwordMatches = passwordHasher.verify(
                request.password(),
                user.getPasswordHash()
        );

        if (!passwordMatches)
        {
            return Result.failure("Invalid credentials.");
        }

        System.out.println("++++++++++on login");
        Session session = sessionManager.startSession(user.getId());

        eventPublisher.publish(new UserLoggedInEvent(
                user.getId(),
                user.getUsername(),
                session.getId(),
                timeProvider.now()
        ));

        AuthResponse response = AuthMapper.toResponse(user, session);
        return Result.success(response);
    }
}
