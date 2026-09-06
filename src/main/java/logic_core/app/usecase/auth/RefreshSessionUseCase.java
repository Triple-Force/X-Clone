package logic_core.app.usecase.auth;

import logic_core.app.dto.request.RefreshSessionRequest;
import logic_core.app.dto.response.AuthResponse;
import logic_core.app.dto.validator.RefreshSessionValidator;
import logic_core.app.mapper.AuthMapper;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.SessionUserContext;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.model.SessionModel;
import logic_core.domain.policy.SessionPolicy;
import logic_core.session.SessionManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshSessionUseCase
{
    @NonNull private final RefreshSessionValidator validator;
    @NonNull private final SessionPolicy policy;
    @NonNull private final SessionManager sessionManager;
    @NonNull private final TimeProvider timeProvider;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<AuthResponse> execute(RefreshSessionRequest request)
    {
        try
        {
            validator.validate(request.refreshToken());

            SessionUserContext context = lockOrchestrator.lockAndGetContextByToken(request.refreshToken());
            SessionModel oldSession = context.session();

            policy.validateRefresh(oldSession, timeProvider.now());

            sessionManager.invalidateSession(oldSession);

            SessionModel newSession = sessionManager.startSession(context.lockedUser().getId());

            return Result.success(AuthMapper.toResponse(context.lockedUser(), newSession));
        }
        catch (Exception e)
        {
            return Result.failure(e.getMessage());
        }
    }
}
