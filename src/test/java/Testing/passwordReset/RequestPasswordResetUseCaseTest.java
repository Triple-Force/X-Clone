package Testing.passwordReset;

import logic_core.app.dto.request.RequestPasswordResetRequest;
import logic_core.app.dto.response.RequestPasswordResetResponse;
import logic_core.app.dto.validator.EmailValidator;
import logic_core.app.service.passwordReset.PasswordResetDeliveryPort;
import logic_core.app.service.passwordReset.PasswordResetOtpService;
import logic_core.app.usecase.auth.RequestPasswordResetUseCase;
import logic_core.common.result.Result;
import logic_core.common.security.PasswordHasher;
import logic_core.common.util.TimeProvider;
import logic_core.domain.model.UserModel;
import logic_core.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("RequestPasswordResetUseCase Tests")
class RequestPasswordResetUseCaseTest
{
    private PasswordResetOtpService otpService;

    @AfterEach
    void tearDown()
    {
        if (otpService != null)
        {
            otpService.close();
        }
    }

    private static TimeProvider fixedTimeProvider()
    {
        return new TimeProvider()
        {
            @Override
            public OffsetDateTime now()
            {
                return OffsetDateTime.parse("2026-07-19T10:00:00Z");
            }
        };
    }

    private PasswordResetOtpService createOtpService()
    {
        otpService = new PasswordResetOtpService(
                fixedTimeProvider(),
                new PasswordHasher(),
                Duration.ofMinutes(10),
                5,
                false
        );
        return otpService;
    }

    @Test
    @DisplayName("null request should fail")
    void nullRequest_fails()
    {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordResetOtpService otp = createOtpService();
        PasswordResetDeliveryPort deliveryPort = mock(PasswordResetDeliveryPort.class);
        EmailValidator emailValidator = new EmailValidator();

        RequestPasswordResetUseCase useCase = new RequestPasswordResetUseCase(
                userRepository, otp, deliveryPort, emailValidator
        );

        Result<RequestPasswordResetResponse> result = useCase.execute(null);

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("existing user should issue OTP and send it")
    void existingUser_issuesAndSendsOtp()
    {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordResetDeliveryPort deliveryPort = mock(PasswordResetDeliveryPort.class);
        PasswordResetOtpService otp = createOtpService();
        EmailValidator emailValidator = new EmailValidator();

        UUID userId = UUID.randomUUID();
        UserModel user = mock(UserModel.class);
        when(user.getId()).thenReturn(userId);

        // Request has mixed case + whitespace; useCase trims to "USER@EXAMPLE.COM"
        when(userRepository.findByEmail("USER@EXAMPLE.COM")).thenReturn(Optional.of(user));

        RequestPasswordResetUseCase useCase = new RequestPasswordResetUseCase(
                userRepository, otp, deliveryPort, emailValidator
        );

        Result<RequestPasswordResetResponse> result = useCase.execute(
                new RequestPasswordResetRequest("  USER@EXAMPLE.COM  ")
        );

        assertTrue(result.isSuccess());
        verify(deliveryPort, times(1)).send(eq("USER@EXAMPLE.COM"), anyString());
    }

    @Test
    @DisplayName("unknown email should still return generic success and not send delivery")
    void unknownEmail_genericSuccess_noDelivery()
    {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordResetDeliveryPort deliveryPort = mock(PasswordResetDeliveryPort.class);
        PasswordResetOtpService otp = createOtpService();
        EmailValidator emailValidator = new EmailValidator();

        // Request has whitespace; useCase trims to "user@example.com"
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        RequestPasswordResetUseCase useCase = new RequestPasswordResetUseCase(
                userRepository, otp, deliveryPort, emailValidator
        );

        Result<RequestPasswordResetResponse> result = useCase.execute(
                new RequestPasswordResetRequest(" user@example.com ")
        );

        assertTrue(result.isSuccess());
        verify(deliveryPort, never()).send(anyString(), anyString());
    }

    @Test
    @DisplayName("invalid email format should fail")
    void invalidEmail_fails()
    {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordResetDeliveryPort deliveryPort = mock(PasswordResetDeliveryPort.class);
        PasswordResetOtpService otp = createOtpService();
        EmailValidator emailValidator = new EmailValidator();

        RequestPasswordResetUseCase useCase = new RequestPasswordResetUseCase(
                userRepository, otp, deliveryPort, emailValidator
        );

        Result<RequestPasswordResetResponse> result = useCase.execute(
                new RequestPasswordResetRequest("not-an-email")
        );

        assertFalse(result.isSuccess());
        verify(deliveryPort, never()).send(anyString(), anyString());
    }
}
