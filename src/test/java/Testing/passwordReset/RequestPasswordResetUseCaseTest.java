package Testing.passwordReset;

import logic_core.app.dto.request.RequestPasswordResetRequest;
import logic_core.app.dto.response.RequestPasswordResetResponse;
import logic_core.app.service.passwordReset.PasswordResetDeliveryPort;
import logic_core.app.service.passwordReset.PasswordResetOtpService;
import logic_core.app.usecase.auth.RequestPasswordResetUseCase;
import logic_core.common.result.Result;
import logic_core.common.security.PasswordHasher;
import logic_core.common.util.TimeProvider;
import logic_core.domain.model.UserModel;
import logic_core.domain.repository.UserRepository;
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

    @Test
    @DisplayName("null request should fail")
    void nullRequest_fails()
    {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordResetOtpService otpService = new PasswordResetOtpService(
                fixedTimeProvider(),
                new PasswordHasher(),
                Duration.ofMinutes(10),
                5,
                false
        );
        PasswordResetDeliveryPort deliveryPort = mock(PasswordResetDeliveryPort.class);

        RequestPasswordResetUseCase useCase = new RequestPasswordResetUseCase(
                userRepository,
                otpService,
                deliveryPort
        );

        Result<RequestPasswordResetResponse> result = useCase.execute(null);

        assertFalse(result.isSuccess());

        otpService.close();
    }

    @Test
    @DisplayName("existing user should issue OTP and send it")
    void existingUser_issuesAndSendsOtp()
    {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordResetDeliveryPort deliveryPort = mock(PasswordResetDeliveryPort.class);

        PasswordResetOtpService otpService = spy(new PasswordResetOtpService(
                fixedTimeProvider(),
                new PasswordHasher(),
                Duration.ofMinutes(10),
                5,
                false
        ));

        UUID userId = UUID.randomUUID();
        UserModel user = mock(UserModel.class);
        when(user.getId()).thenReturn(userId);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        RequestPasswordResetUseCase useCase = new RequestPasswordResetUseCase(
                userRepository,
                otpService,
                deliveryPort
        );

        Result<RequestPasswordResetResponse> result = useCase.execute(
                new RequestPasswordResetRequest("  USER@EXAMPLE.COM  ")
        );

        assertTrue(result.isSuccess());
        verify(deliveryPort, times(1)).send(eq("user@example.com"), anyString());

        otpService.close();
    }

    @Test
    @DisplayName("unknown email should still return generic success and not send delivery")
    void unknownEmail_genericSuccess_noDelivery()
    {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordResetDeliveryPort deliveryPort = mock(PasswordResetDeliveryPort.class);

        PasswordResetOtpService otpService = new PasswordResetOtpService(
                fixedTimeProvider(),
                new PasswordHasher(),
                Duration.ofMinutes(10),
                5,
                false
        );

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        RequestPasswordResetUseCase useCase = new RequestPasswordResetUseCase(
                userRepository,
                otpService,
                deliveryPort
        );

        Result<RequestPasswordResetResponse> result = useCase.execute(
                new RequestPasswordResetRequest(" user@example.com ")
        );

        assertTrue(result.isSuccess());
        verify(deliveryPort, never()).send(anyString(), anyString());

        otpService.close();
    }
}
