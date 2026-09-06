package Testing.passwordReset;

import logic_core.app.dto.request.ResetPasswordRequest;
import logic_core.app.dto.response.ResetPasswordResponse;
import logic_core.app.dto.validator.EmailValidator;
import logic_core.app.dto.validator.PasswordValidator;
import logic_core.app.service.passwordReset.OtpVerifyStatus;
import logic_core.app.service.passwordReset.PasswordResetOtpService;
import logic_core.app.usecase.auth.ResetPasswordUseCase;
import logic_core.common.result.Result;
import logic_core.common.security.PasswordHasher;
import logic_core.domain.model.UserModel;
import logic_core.domain.repository.UserRepository;
import logic_core.session.SessionManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("ResetPasswordUseCase Tests")
class ResetPasswordUseCaseTest
{
    @Test
    @DisplayName("null request should fail")
    void nullRequest_fails()
    {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordResetOtpService otpService = mock(PasswordResetOtpService.class);
        PasswordHasher passwordHasher = new PasswordHasher();
        SessionManager sessionManager = mock(SessionManager.class);
        EmailValidator emailValidator = new EmailValidator();
        PasswordValidator passwordValidator = new PasswordValidator();

        ResetPasswordUseCase useCase = new ResetPasswordUseCase(
                userRepository, otpService, passwordHasher, sessionManager,
                emailValidator, passwordValidator
        );

        Result<ResetPasswordResponse> result = useCase.execute(null);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("invalid OTP should stop the flow before password update")
    void invalidOtp_stopsBeforeUpdate()
    {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordResetOtpService otpService = mock(PasswordResetOtpService.class);
        PasswordHasher passwordHasher = new PasswordHasher();
        SessionManager sessionManager = mock(SessionManager.class);
        EmailValidator emailValidator = new EmailValidator();
        PasswordValidator passwordValidator = new PasswordValidator();

        when(otpService.verify("user@example.com", "123456")).thenReturn(OtpVerifyStatus.INVALID_CODE);

        ResetPasswordUseCase useCase = new ResetPasswordUseCase(
                userRepository, otpService, passwordHasher, sessionManager,
                emailValidator, passwordValidator
        );

        Result<ResetPasswordResponse> result = useCase.execute(
                new ResetPasswordRequest("user@example.com", "newPassword123", "123456")
        );

        assertFalse(result.isSuccess());
        verify(userRepository, never()).update(any());
        verify(sessionManager, never()).revokeAllForUser(any());
        verify(otpService, never()).consumeIfVerified(anyString());
    }

    @Test
    @DisplayName("successful reset should revoke sessions, update password, and start new session")
    void successfulReset_happyPath()
    {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordResetOtpService otpService = mock(PasswordResetOtpService.class);
        PasswordHasher passwordHasher = new PasswordHasher();
        SessionManager sessionManager = mock(SessionManager.class);
        EmailValidator emailValidator = new EmailValidator();
        PasswordValidator passwordValidator = new PasswordValidator();

        UUID userId = UUID.randomUUID();
        UserModel user = mock(UserModel.class);

        String email = "user@example.com";
        String code = "123456";
        String newPassword = "NewPassword123!";

        when(otpService.verify(email, code)).thenReturn(OtpVerifyStatus.OK);
        when(otpService.getVerifiedUserId(email)).thenReturn(Optional.of(userId));

        when(otpService.consumeIfVerified(email))
                .thenReturn(PasswordResetOtpService.ConsumeResult.ok(userId));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn(email);

        ResetPasswordUseCase useCase = new ResetPasswordUseCase(
                userRepository, otpService, passwordHasher, sessionManager,
                emailValidator, passwordValidator
        );

        Result<ResetPasswordResponse> result = useCase.execute(
                new ResetPasswordRequest(email, code, newPassword)
        );

        assertTrue(result.isSuccess(), "Result should be success but was: " + result.getMessage());
        assertNotNull(result.getData());
        assertTrue(result.getData().isReset());

        verify(sessionManager).revokeAllForUser(userId);
        verify(userRepository).update(user);
        verify(otpService).consumeIfVerified(email);
        verify(sessionManager).startSession(userId);
    }
}