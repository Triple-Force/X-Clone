package Testing.passwordReset;

import logic_core.app.service.passwordReset.OtpVerifyStatus;
import logic_core.app.service.passwordReset.PasswordResetOtpService;
import logic_core.common.security.PasswordHasher;
import logic_core.common.util.TimeProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordResetOtpService Tests")
class PasswordResetOtpServiceTest
{
    private PasswordResetOtpService service;

    @AfterEach
    void tearDown()
    {
        if (service != null)
        {
            service.close();
        }
    }

    @Test
    @DisplayName("issue() should generate exactly 6 numeric digits")
    void issue_generatesSixNumericDigits()
    {
        TimeProvider timeProvider = new FixedTimeProvider(OffsetDateTime.parse("2026-07-19T10:00:00Z"));
        PasswordHasher passwordHasher = new PasswordHasher();

        service = new PasswordResetOtpService(
                timeProvider,
                passwordHasher,
                Duration.ofMinutes(10),
                5,
                false
        );

        String otp = service.issue("User@Email.com", UUID.randomUUID());

        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
    }

    @Test
    @DisplayName("verify() with correct OTP should return OK and mark entry verified")
    void verify_correctCode_returnsOk()
    {
        TimeProvider timeProvider = new FixedTimeProvider(OffsetDateTime.parse("2026-07-19T10:00:00Z"));
        PasswordHasher passwordHasher = new PasswordHasher();

        service = new PasswordResetOtpService(
                timeProvider,
                passwordHasher,
                Duration.ofMinutes(10),
                5,
                false
        );

        UUID userId = UUID.randomUUID();
        String email = "user@example.com";
        String otp = service.issue(email, userId);

        OtpVerifyStatus status = service.verify(email, otp);

        assertEquals(OtpVerifyStatus.OK, status);
        assertEquals(Optional.of(userId), service.getVerifiedUserId(email));
    }

    @Test
    @DisplayName("verify() with wrong OTP should return INVALID_CODE and increment attempts")
    void verify_wrongCode_returnsInvalidCode()
    {
        TimeProvider timeProvider = new FixedTimeProvider(OffsetDateTime.parse("2026-07-19T10:00:00Z"));
        PasswordHasher passwordHasher = new PasswordHasher();

        service = new PasswordResetOtpService(
                timeProvider,
                passwordHasher,
                Duration.ofMinutes(10),
                5,
                false
        );

        service.issue("user@example.com", UUID.randomUUID());

        OtpVerifyStatus status = service.verify("user@example.com", "000000");

        assertEquals(OtpVerifyStatus.INVALID_CODE, status);
    }

    @Test
    @DisplayName("verify() on expired OTP should return EXPIRED and remove entry")
    void verify_expiredCode_returnsExpired() {
        OffsetDateTime issuedAt = OffsetDateTime.parse("2026-07-19T10:00:00Z");
        OffsetDateTime expiredAt = issuedAt.plusMinutes(11);

        FixedTimeProvider timeProvider = new FixedTimeProvider(issuedAt);
        PasswordHasher passwordHasher = new PasswordHasher();

        service = new PasswordResetOtpService(
                timeProvider,
                passwordHasher,
                Duration.ofMinutes(10),
                5,
                false
        );

        service.issue("userEmail@gmail.com", UUID.randomUUID());

        timeProvider.setNow(expiredAt);

        OtpVerifyStatus status = service.verify("userEmail@gmail.com", "000000");

        assertEquals(OtpVerifyStatus.EXPIRED, status);
        assertEquals(Optional.empty(), service.getVerifiedUserId("userEmail@gmail.com"));
    }


    @Test
    @DisplayName("verify() should return TOO_MANY_ATTEMPTS after limit")
    void verify_tooManyAttempts_returnsTooManyAttempts() {
        TimeProvider timeProvider = new FixedTimeProvider(OffsetDateTime.parse("2026-07-19T10:00:00Z"));
        PasswordHasher passwordHasher = new PasswordHasher();

        service = new PasswordResetOtpService(
                timeProvider,
                passwordHasher,
                Duration.ofMinutes(10),
                5,
                false
        );

        service.issue("user@example.com", UUID.randomUUID());

        OtpVerifyStatus last = null;
        for (int i = 0; i < 6; i++) {
            last = service.verify("user@example.com", "111111");
        }

        assertEquals(OtpVerifyStatus.TOO_MANY_ATTEMPTS, last);
    }


    @Test
    @DisplayName("consumeIfVerified() should succeed only after verification")
    void consumeIfVerified_succeedsOnlyAfterVerification()
    {
        TimeProvider timeProvider = new FixedTimeProvider(OffsetDateTime.parse("2026-07-19T10:00:00Z"));
        PasswordHasher passwordHasher = new PasswordHasher();

        service = new PasswordResetOtpService(
                timeProvider,
                passwordHasher,
                Duration.ofMinutes(10),
                5,
                false
        );

        String email = "user@example.com";
        String otp = service.issue(email, UUID.randomUUID());

        assertFalse(service.consumeIfVerified(email).isSuccess());

        assertEquals(OtpVerifyStatus.OK, service.verify(email, otp));

        assertTrue(service.consumeIfVerified(email).isSuccess());
        assertFalse(service.consumeIfVerified(email).isSuccess());
    }

    @Test
    @DisplayName("invalidate() should remove OTP entry")
    void invalidate_removesEntry()
    {
        TimeProvider timeProvider = new FixedTimeProvider(OffsetDateTime.parse("2026-07-19T10:00:00Z"));
        PasswordHasher passwordHasher = new PasswordHasher();

        service = new PasswordResetOtpService(
                timeProvider,
                passwordHasher,
                Duration.ofMinutes(10),
                5,
                false
        );

        service.issue("user@example.com", UUID.randomUUID());
        service.invalidate("user@example.com");

        assertEquals(OtpVerifyStatus.NOT_FOUND, service.verify("user@example.com", "123456"));
    }

    @Test
    @DisplayName("verify() with mixed-case email should return correct status")
    void verify_mixedCaseEmail_returnsCorrectStatus()
    {
        TimeProvider timeProvider = new FixedTimeProvider(OffsetDateTime.parse("2026-07-19T10:00:00Z"));
        PasswordHasher passwordHasher = new PasswordHasher();

        service = new PasswordResetOtpService(
                timeProvider,
                passwordHasher,
                Duration.ofMinutes(10),
                5,
                false
        );

        UUID userId = UUID.randomUUID();
        String otp = service.issue("User@Example.com", userId);

        // Verify with different casing — should still find the entry
        OtpVerifyStatus status = service.verify("user@example.com", otp);
        assertEquals(OtpVerifyStatus.OK, status);
        assertEquals(Optional.of(userId), service.getVerifiedUserId("USER@EXAMPLE.COM"));
    }

    @Test
    @DisplayName("verify() with untrimmed email should return correct status")
    void verify_untrimmedEmail_returnsCorrectStatus()
    {
        TimeProvider timeProvider = new FixedTimeProvider(OffsetDateTime.parse("2026-07-19T10:00:00Z"));
        PasswordHasher passwordHasher = new PasswordHasher();

        service = new PasswordResetOtpService(
                timeProvider,
                passwordHasher,
                Duration.ofMinutes(10),
                5,
                false
        );

        UUID userId = UUID.randomUUID();
        String otp = service.issue("  user@example.com  ", userId);

        // Verify with surrounding whitespace — should still find the entry
        OtpVerifyStatus status = service.verify(" user@example.com ", otp);
        assertEquals(OtpVerifyStatus.OK, status);
    }

    @Test
    @DisplayName("verify() expired OTP with mixed-case email should return EXPIRED")
    void verify_expiredMixedCaseEmail_returnsExpired()
    {
        OffsetDateTime issuedAt = OffsetDateTime.parse("2026-07-19T10:00:00Z");
        OffsetDateTime expiredAt = issuedAt.plusMinutes(11);

        FixedTimeProvider timeProvider = new FixedTimeProvider(issuedAt);
        PasswordHasher passwordHasher = new PasswordHasher();

        service = new PasswordResetOtpService(
                timeProvider,
                passwordHasher,
                Duration.ofMinutes(10),
                5,
                false
        );

        service.issue("User@Email.com", UUID.randomUUID());

        timeProvider.setNow(expiredAt);

        // Verify with different casing — should find entry and return EXPIRED
        OtpVerifyStatus status = service.verify("user@EMAIL.com", "000000");
        assertEquals(OtpVerifyStatus.EXPIRED, status);
    }

    private static final class FixedTimeProvider extends TimeProvider
    {
        private OffsetDateTime now;

        private FixedTimeProvider(OffsetDateTime now)
        {
            this.now = now;
        }

        @Override
        public OffsetDateTime now()
        {
            return now;
        }

        void setNow(OffsetDateTime now)
        {
            this.now = now;
        }
    }

}
