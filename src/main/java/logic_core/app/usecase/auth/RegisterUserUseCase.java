package logic_core.app.usecase.auth;

import Shared.Models.Session.Session;
import logic_core.app.dto.request.RegisterRequest;
import logic_core.app.dto.response.AuthResponse;
import logic_core.app.dto.validator.RegisterValidator;
import logic_core.app.mapper.AuthMapper;
import logic_core.common.result.Result;
import logic_core.common.security.PasswordHasher;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.event.userEvent.UserRegisteredEvent;
import logic_core.domain.model.UserModel;
import logic_core.domain.policy.RegistrationPolicy;
import logic_core.domain.repository.UserRepository;
import logic_core.session.SessionManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class RegisterUserUseCase
{
    @NonNull private final RegisterValidator validator;
    @NonNull private final RegistrationPolicy policy;
    @NonNull private final UserRepository userRepository;
    @NonNull private final EventPublisher eventPublisher;
    @NonNull private final PasswordHasher passwordHasher;
    @NonNull private final TimeProvider timeProvider;
    @NonNull private final SessionManager sessionManager;

    public Result<AuthResponse> execute(RegisterRequest request)
    {
        try
        {
            validator.validate(
                    request.username(),
                    request.password(),
                    request.email()
            );

            policy.validate(
                    request.username(),
                    request.email()
            );
        }
        catch (RuntimeException e)
        {
            return Result.failure(e.getMessage());
        }

        UserModel user = createUser(request);
        UserModel persistedUser =
                userRepository.save(user).orElseThrow();

        return Result.success(registerAndAuthenticate(persistedUser));
    }


    private UserModel createUser(RegisterRequest request)
    {
        String passwordHash = passwordHasher.hash(request.password());

        return UserModel.createNew(
                null,
                request.username(),
                request.email(),
                passwordHash,
                timeProvider.now()
        );
    }

    private AuthResponse registerAndAuthenticate(UserModel user)
    {

        System.out.println("publish event. in registerAndAuthenticate");
        eventPublisher.publish(new UserRegisteredEvent(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                timeProvider.now()
        ));


        System.out.println("++++++++++on register");
        Session session = sessionManager.startSession(user.getId());
        return AuthMapper.toResponse(user, session);
    }
}
