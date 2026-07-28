package logic_core.app.usecase.auth;

import Shared.Models.Session.Session;
import jakarta.transaction.Transactional;
import logic_core.app.dto.request.LogoutRequest;
import logic_core.app.dto.response.LogoutResponse;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.SessionUserContext;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.event.authentication.UserLoggedOutEvent;
import logic_core.domain.model.UserModel;
import logic_core.session.SessionManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LogoutUserUseCase
{
    @NonNull private final SessionManager sessionManager;
    @NonNull private final EventPublisher eventPublisher;
    @NonNull private final TimeProvider timeProvider;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<LogoutResponse> execute(LogoutRequest request)
    {
        try
        {
            SessionUserContext context = lockOrchestrator.lockAndGetContextByToken(request.sessionToken());
            UserModel lockedUser = context.lockedUser();
            Session sessionToInvalidate = context.session();

            sessionManager.invalidateSession(sessionToInvalidate);

            eventPublisher.publish(new UserLoggedOutEvent(
                    lockedUser.getId(),
                    lockedUser.getUsername(),
                    sessionToInvalidate.getId(),
                    timeProvider.now()
            ));

            LogoutResponse response = new LogoutResponse(true, "Logged out successfully.");
            return Result.success(response);
        }
        catch (Exception e)
        {
            return Result.failure(e.getMessage());
        }
    }
}
