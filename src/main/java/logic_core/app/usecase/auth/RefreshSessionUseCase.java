package logic_core.app.usecase.auth;

import Shared.Models.Session.Session;
import logic_core.app.dto.request.RefreshSessionRequest;
import logic_core.app.dto.response.AuthResponse;
import logic_core.app.dto.validator.RefreshSessionValidator;
import logic_core.app.mapper.AuthMapper;
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
    @NonNull private final UserRepository userRepository;

    public Result<AuthResponse> execute(RefreshSessionRequest request)
    {
        try
        {
            validator.validate(request.refreshToken());

            Session oldSession = sessionManager.findByToken(request.refreshToken())
                    .orElseThrow(() -> new RuntimeException("Session not found for provided token."));

            policy.validateRefresh(oldSession, timeProvider.now());

            UserModel user = userRepository.findUserBySessionId(oldSession.getId())
                    .orElseThrow(() -> new RuntimeException("User not found."));

            sessionManager.invalidateSession(oldSession);

            Session newSession = sessionManager.startSession(user.getId());

            eventPublisher.publish(new SessionRefreshedEvent(
                    user.getId(),
                    oldSession.getId(),
                    newSession.getId(),
                    timeProvider.now()
            ));

            return Result.success(AuthMapper.toResponse(user, newSession));
        }
        catch (Exception e)
        {
            return Result.failure(e.getMessage());
        }
    }
}
