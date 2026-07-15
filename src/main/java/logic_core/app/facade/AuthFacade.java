package logic_core.app.facade;

import logic_core.app.dto.request.LoginRequest;
import logic_core.app.dto.request.LogoutRequest;
import logic_core.app.dto.request.RefreshSessionRequest;
import logic_core.app.dto.request.RegisterRequest;
import logic_core.app.dto.response.AuthResponse;
import logic_core.app.dto.response.LogoutResponse;
import logic_core.app.usecase.auth.LoginUserUseCase;
import logic_core.app.usecase.auth.LogoutUserUseCase;
import logic_core.app.usecase.auth.RefreshSessionUseCase;
import logic_core.app.usecase.auth.RegisterUserUseCase;
import logic_core.common.result.Result;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthFacade
{
    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final LogoutUserUseCase logoutUserUseCase;
    private final RefreshSessionUseCase refreshSessionUseCase;

    public Result<AuthResponse> register(RegisterRequest request)
    {
        return registerUserUseCase.execute(request);
    }

    public Result<AuthResponse> login(LoginRequest request)
    {
        return loginUserUseCase.execute(request);
    }

    public Result<LogoutResponse> logout(LogoutRequest request)
    {
        return logoutUserUseCase.execute(request);
    }

    public Result<AuthResponse> refresh(RefreshSessionRequest request)
    {
        return refreshSessionUseCase.execute(request);
    }
}
