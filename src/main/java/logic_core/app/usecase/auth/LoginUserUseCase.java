package logic_core.app.usecase.auth;

import logic_core.app.dto.request.LoginRequest;
import logic_core.app.dto.response.AuthResponse;
import logic_core.app.dto.validator.LoginValidator;
import logic_core.app.mapper.AuthMapper;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.SessionUserContext;
import logic_core.common.result.Result;
import logic_core.common.security.PasswordHasher;
import logic_core.common.util.TimeProvider;
import logic_core.domain.model.SessionModel;
import logic_core.domain.model.UserModel;
import logic_core.session.SessionManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginUserUseCase
{
    @NonNull private final LoginValidator validator;
    @NonNull private final PasswordHasher passwordHasher;
    @NonNull private final SessionManager sessionManager;
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

        SessionModel session = sessionManager.startSession(lockedUser.getId());


        return Result.success(AuthMapper.toResponse(lockedUser, session));
    }
}
