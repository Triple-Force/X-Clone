package logic_core.app.usecase.auth;

import logic_core.app.dto.request.ResetPasswordRequest;
import logic_core.app.dto.response.ResetPasswordResponse;
import logic_core.app.dto.validator.EmailValidator;
import logic_core.app.dto.validator.PasswordValidator;
import logic_core.app.service.passwordReset.OtpVerifyStatus;
import logic_core.app.service.passwordReset.PasswordResetOtpService;
import logic_core.common.exception.ValidationException;
import logic_core.common.result.Result;
import logic_core.common.security.PasswordHasher;
import logic_core.common.util.TimeProvider;
import logic_core.domain.model.SessionModel;
import logic_core.domain.model.UserModel;
import logic_core.domain.repository.UserRepository;
import logic_core.session.SessionManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResetPasswordUseCase
{
    @NonNull private final UserRepository userRepository;
    @NonNull private final PasswordResetOtpService otpService;
    @NonNull private final PasswordHasher passwordHasher;
    @NonNull private final SessionManager sessionManager;
    @NonNull private final EmailValidator emailValidator;
    @NonNull private final PasswordValidator passwordValidator;

    @Transactional
    public Result<ResetPasswordResponse> execute(ResetPasswordRequest request)
    {
        if (request == null)
        {
            return Result.failure("Invalid request.");
        }

        final String email = request.email().trim();
        final String code = request.code();
        final String newPassword = request.newPassword();

        try
        {
            emailValidator.validate(email);
            passwordValidator.validate(newPassword);
            validateCode(code);
        }
        catch (ValidationException | IllegalArgumentException ex)
        {
            return Result.failure(ex.getMessage());
        }

        OtpVerifyStatus verifyStatus = otpService.verify(email, code);
        if (verifyStatus != OtpVerifyStatus.OK)
        {
            return Result.failure(mapVerifyFailure(verifyStatus));
        }

        Optional<UUID> userIdOpt = otpService.getVerifiedUserId(email);

        if (userIdOpt.isEmpty())
        {
            otpService.invalidate(email);
            return Result.failure("Invalid or expired code.");
        }

        Optional<UserModel> userOpt = userRepository.findById(userIdOpt.get());

        if (userOpt.isEmpty())
        {
            otpService.invalidate(email);
            return Result.failure("Invalid or expired code.");
        }

        UserModel user = userOpt.get();

        try
        {
            sessionManager.revokeAllForUser(user.getId());
        }
        catch (RuntimeException ex)
        {
            return Result.failure("Unable to reset password right now. Please try again.");
        }

        final String passwordHash = passwordHasher.hash(newPassword);
        user.updatePasswordHash(passwordHash);

        try
        {
            userRepository.update(user);
        }
        catch (RuntimeException ex)
        {
            return Result.failure("Unable to reset password right now. Please try again.");
        }

        PasswordResetOtpService.ConsumeResult consumeResult = otpService.consumeIfVerified(email);
        if (!consumeResult.isSuccess())
        {
            return Result.failure(mapConsumeFailure(consumeResult.getStatus()));
        }

        ResetPasswordResponse response = authenticate(user);
        return Result.success(response);
    }

    private String mapVerifyFailure(OtpVerifyStatus status)
    {
        return switch (status)
        {
            case OK -> "Password reset failed.";
            case NOT_FOUND, INVALID_CODE -> "Invalid or expired code.";
            case EXPIRED -> "Reset code has expired.";
            case TOO_MANY_ATTEMPTS -> "Too many attempts. Please request a new code.";
        };
    }

    private String mapConsumeFailure(OtpVerifyStatus status)
    {
        return switch (status)
        {
            case OK -> "Password reset failed.";
            case NOT_FOUND, INVALID_CODE -> "Invalid or expired code.";
            case EXPIRED -> "Reset code has expired.";
            case TOO_MANY_ATTEMPTS -> "Too many attempts. Please request a new code.";
        };
    }

    private void validateCode(String code)
    {
        if (code == null || code.isBlank())
        {
            throw new IllegalArgumentException("Invalid code.");
        }
    }

    private ResetPasswordResponse authenticate(UserModel user)
    {


        SessionModel session = sessionManager.startSession(user.getId());

        return new ResetPasswordResponse(true);
    }
}
