package logic_core.app.facade;

import logic_core.app.dto.request.*;
import logic_core.app.dto.response.*;
import logic_core.app.usecase.auth.*;
import logic_core.common.result.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthFacade
{
    @NonNull private final RegisterUserUseCase registerUserUseCase;
    @NonNull private final LoginUserUseCase loginUserUseCase;
    @NonNull private final LogoutUserUseCase logoutUserUseCase;
    @NonNull private final RefreshSessionUseCase refreshSessionUseCase;
    @NonNull private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    @NonNull private final VerifyPasswordResetCodeUseCase verifyPasswordResetCodeUseCase;
    @NonNull private final ResetPasswordUseCase resetPasswordUseCase;

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

    public Result<RequestPasswordResetResponse> requestPasswordReset(RequestPasswordResetRequest request)
    {
        return requestPasswordResetUseCase.execute(request);
    }

    public Result<VerifyPasswordResetCodeResponse> verifyPasswordResetCode(VerifyPasswordResetCodeRequest request)
    {
        return verifyPasswordResetCodeUseCase.execute(request);
    }

    public Result<ResetPasswordResponse> resetPassword(ResetPasswordRequest request)
    {
        return resetPasswordUseCase.execute(request);
    }
}
