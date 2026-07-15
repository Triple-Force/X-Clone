package logic_core.app.usecase.auth;

import Shared.Models.Session.Session;
import logic_core.app.dto.request.LogoutRequest;
import logic_core.app.dto.response.LogoutResponse;
import logic_core.common.exception.NotFoundException;
import logic_core.common.result.Result;
import logic_core.common.util.TimeProvider;
import logic_core.domain.event.EventPublisher;
import logic_core.domain.event.authentication.UserLoggedOutEvent;
import logic_core.domain.model.UserModel;
import logic_core.domain.repository.UserRepository;
import logic_core.session.SessionManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class LogoutUserUseCase
{
    @NonNull private final SessionManager sessionManager;
    @NonNull private final EventPublisher eventPublisher;
    @NonNull private final TimeProvider timeProvider;
    @NonNull private final UserRepository repository;

    public Result<LogoutResponse> execute(LogoutRequest request)
    {
        return sessionManager.findByToken(request.sessionToken())
                .map(this::logout)
                .orElseGet(() -> Result.failure("Invalid session."));
    }

    private Result<LogoutResponse> logout(Session session)
    {
        UserModel userModel = repository.findUserBySessionId(session.getId())
                .orElseThrow(()-> new NotFoundException("user not found."));

        UUID sessionId = session.getId();

        sessionManager.invalidateSession(session);

        eventPublisher.publish(new UserLoggedOutEvent(
                userModel.getId(),
                userModel.getUsername(),
                sessionId,
                timeProvider.now()
        ));

        LogoutResponse response =
                new LogoutResponse(true,"Logged out successfully.");

        return Result.success(response);
    }
}
