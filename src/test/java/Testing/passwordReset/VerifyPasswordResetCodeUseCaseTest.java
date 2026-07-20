package Testing.passwordReset;

import logic_core.app.dto.request.VerifyPasswordResetCodeRequest;
import logic_core.app.dto.response.VerifyPasswordResetCodeResponse;
import logic_core.app.service.passwordReset.OtpVerifyStatus;
import logic_core.app.service.passwordReset.PasswordResetOtpService;
import logic_core.app.usecase.auth.VerifyPasswordResetCodeUseCase;
import logic_core.common.result.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("VerifyPasswordResetCodeUseCase Tests")
class VerifyPasswordResetCodeUseCaseTest
{
    @Test
    @DisplayName("null request should fail")
    void nullRequest_fails()
    {
        PasswordResetOtpService otpService = mock(PasswordResetOtpService.class);
        VerifyPasswordResetCodeUseCase useCase = new VerifyPasswordResetCodeUseCase(otpService);

        Result<VerifyPasswordResetCodeResponse> result = useCase.execute(null);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("correct code should verify successfully")
    void correctCode_verifiesSuccessfully()
    {
        PasswordResetOtpService otpService = mock(PasswordResetOtpService.class);
        when(otpService.verify("user@example.com", "123456")).thenReturn(OtpVerifyStatus.OK);

        VerifyPasswordResetCodeUseCase useCase = new VerifyPasswordResetCodeUseCase(otpService);

        Result<VerifyPasswordResetCodeResponse> result = useCase.execute(
                new VerifyPasswordResetCodeRequest(" user@example.com ", "123456")
        );

        assertTrue(result.isSuccess());
        verify(otpService, times(1)).verify("user@example.com", "123456");
    }

    @Test
    @DisplayName("expired code should return expiration message")
    void expiredCode_returnsExpirationMessage()
    {
        PasswordResetOtpService otpService = mock(PasswordResetOtpService.class);
        when(otpService.verify("user@example.com", "123456")).thenReturn(OtpVerifyStatus.EXPIRED);

        VerifyPasswordResetCodeUseCase useCase = new VerifyPasswordResetCodeUseCase(otpService);

        Result<VerifyPasswordResetCodeResponse> result = useCase.execute(
                new VerifyPasswordResetCodeRequest("user@example.com", "123456")
        );

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("verify should not consume OTP")
    void verify_doesNotConsumeOtp()
    {
        PasswordResetOtpService otpService = mock(PasswordResetOtpService.class);
        when(otpService.verify(anyString(), anyString())).thenReturn(OtpVerifyStatus.OK);

        VerifyPasswordResetCodeUseCase useCase = new VerifyPasswordResetCodeUseCase(otpService);

        Result<VerifyPasswordResetCodeResponse> result = useCase.execute(
                new VerifyPasswordResetCodeRequest("user@example.com", "123456")
        );

        assertTrue(result.isSuccess());
        verify(otpService, never()).consumeIfVerified(anyString());
    }
}
