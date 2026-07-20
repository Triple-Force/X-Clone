package Testing.passwordReset;

import logic_core.app.service.passwordReset.OtpVerifyStatus;
import logic_core.app.service.passwordReset.PasswordResetOtpService;
import logic_core.common.security.PasswordHasher;
import logic_core.common.util.TimeProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordReset Concurrency Tests")
class PasswordResetConcurrencyTest
{
    @Test
    @DisplayName("only one concurrent consumer should succeed")
    void onlyOneConcurrentConsumeSucceeds() throws Exception
    {
        PasswordResetOtpService service = new PasswordResetOtpService(
                new TimeProvider()
                {
                    @Override
                    public OffsetDateTime now()
                    {
                        return OffsetDateTime.parse("2026-07-19T10:00:00Z");
                    }
                },
                new PasswordHasher(),
                Duration.ofMinutes(10),
                5,
                false
        );

        String email = "user@example.com";
        String otp = service.issue(email, UUID.randomUUID());

        assertEquals(OtpVerifyStatus.OK, service.verify(email, otp));

        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch ready = new CountDownLatch(8);
        CountDownLatch start = new CountDownLatch(1);

        ConcurrentLinkedQueue<Boolean> results = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < 8; i++)
        {
            executor.submit(() -> {
                ready.countDown();
                try
                {
                    start.await();
                    results.add(service.consumeIfVerified(email).isSuccess());
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
            });
        }

        assertTrue(ready.await(5, TimeUnit.SECONDS));
        start.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        long successCount = results.stream().filter(Boolean::booleanValue).count();
        assertEquals(1, successCount);

        service.close();
    }
}
