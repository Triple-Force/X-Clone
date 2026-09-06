package logic_core.app.usecase.auth;

import logic_core.app.dto.request.VerifyPasswordResetCodeRequest;
import logic_core.app.dto.response.VerifyPasswordResetCodeResponse;
import logic_core.app.service.passwordReset.OtpVerifyStatus;
import logic_core.app.service.passwordReset.PasswordResetOtpService;
import logic_core.common.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerifyPasswordResetCodeUseCase
{
    private final PasswordResetOtpService otpService;

    public VerifyPasswordResetCodeUseCase(PasswordResetOtpService otpService)
    {
        this.otpService = otpService;
    }

    @Transactional
    public Result<VerifyPasswordResetCodeResponse> execute(VerifyPasswordResetCodeRequest request)
    {
        if (request == null)
        {
            return Result.failure("Invalid request.");
        }

        final String email = request.email().trim();

        if (email.isEmpty() || request.code() == null || request.code().isBlank())
        {
            return Result.failure("Invalid or expired code.");

        }

        OtpVerifyStatus status = otpService.verify(email, request.code());

        return switch (status)
        {
            case OK -> Result.success(
                    new VerifyPasswordResetCodeResponse(true, "Code verified.")
            );
            case NOT_FOUND, INVALID_CODE -> Result.failure("Invalid or expired code.");
            case EXPIRED -> Result.failure("Code has expired. Please request a new one.");
            case TOO_MANY_ATTEMPTS -> Result.failure(
                    "Too many attempts. Please request a new code."
            );
        };
    }
}