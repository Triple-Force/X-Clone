package logic_core.app.usecase.auth;

import logic_core.app.dto.request.LogoutRequest;
import logic_core.app.dto.response.LogoutResponse;
import logic_core.app.security.AuthLockOrchestrator;
import logic_core.app.security.SessionUserContext;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.model.SessionModel;
import logic_core.session.SessionManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutUserUseCase
{
    @NonNull private final SessionManager sessionManager;
    @NonNull private final AuthLockOrchestrator lockOrchestrator;

    @Transactional
    public Result<LogoutResponse> execute(LogoutRequest request)
    {
        try
        {
            SessionUserContext context = lockOrchestrator.lockAndGetContextByToken(request.sessionToken());
            SessionModel sessionToInvalidate = context.session();

            sessionManager.invalidateSession(sessionToInvalidate);


            LogoutResponse response = new LogoutResponse(true, "Logged out successfully.");
            return Result.success(response);
        }
        catch (Exception e)
        {
            return Result.failure(e.getMessage());
        }
    }
}
