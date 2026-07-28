package logic_core.app.usecase.auth;

import Shared.Models.Session.Session;
import jakarta.transaction.Transactional;
import logic_core.app.dto.request.RefreshSessionRequest;
import logic_core.app.dto.response.AuthResponse;
import logic_core.app.dto.validator.RefreshSessionValidator;
import logic_core.app.mapper.AuthMapper;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.SessionUserContext;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.event.authentication.SessionRefreshedEvent;
import logic_core.domain.model.UserModel;
import logic_core.domain.policy.SessionPolicy;
import logic_core.domain.repository.UserRepository;
import logic_core.session.SessionManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RefreshSessionUseCase
{
    @NonNull private final RefreshSessionValidator validator;
    @NonNull private final SessionPolicy policy;
    @NonNull private final SessionManager sessionManager;
    @NonNull private final EventPublisher eventPublisher;
    @NonNull private final TimeProvider timeProvider;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<AuthResponse> execute(RefreshSessionRequest request)
    {
        try
        {
            validator.validate(request.refreshToken());

            SessionUserContext context = lockOrchestrator.lockAndGetContextByToken(request.refreshToken());
            Session oldSession = context.session();

            policy.validateRefresh(oldSession, timeProvider.now());

            sessionManager.invalidateSession(oldSession);

            Session newSession = sessionManager.startSession(context.lockedUser().getId());

            eventPublisher.publish(new SessionRefreshedEvent(
                    context.lockedUser().getId(),
                    oldSession.getId(),
                    newSession.getId(),
                    timeProvider.now()
            ));

            return Result.success(AuthMapper.toResponse(context.lockedUser(), newSession));
        }
        catch (Exception e)
        {
            return Result.failure(e.getMessage());
        }
    }
}
