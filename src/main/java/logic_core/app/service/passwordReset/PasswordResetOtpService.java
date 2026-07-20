package logic_core.app.service.passwordReset;

import logic_core.common.security.PasswordHasher;
import logic_core.common.util.TimeProvider;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * App-scoped in-memory store for password-reset OTPs.
 * Must be a single shared instance (like EventBus), never request-scoped.
 *
 * Thread-safe. Survives client reconnects within the same JVM.
 * Does NOT survive process restart.
 */
public final class PasswordResetOtpService implements AutoCloseable
{
    public static final int OTP_LENGTH = 6;
    public static final int MAX_ATTEMPTS = 5;
    public static final Duration DEFAULT_TTL = Duration.ofMinutes(10);
    public static final Duration CLEANUP_INTERVAL = Duration.ofMinutes(1);

    private final ConcurrentHashMap<String, OtpEntry> store = new ConcurrentHashMap<>();
    private final TimeProvider timeProvider;
    private final PasswordHasher passwordHasher;
    private final SecureRandom secureRandom;
    private final Duration ttl;
    private final int maxAttempts;

    private final ScheduledExecutorService cleanupExecutor;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public PasswordResetOtpService(TimeProvider timeProvider, PasswordHasher passwordHasher)
    {
        this(timeProvider, passwordHasher, DEFAULT_TTL, MAX_ATTEMPTS, true);
    }

    public PasswordResetOtpService(
            TimeProvider timeProvider,
            PasswordHasher passwordHasher,
            Duration ttl,
            int maxAttempts,
            boolean startCleanupScheduler)
    {
        this.timeProvider = Objects.requireNonNull(timeProvider, "timeProvider");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher");
        this.secureRandom = new SecureRandom();
        this.ttl = Objects.requireNonNull(ttl, "ttl");
        this.maxAttempts = maxAttempts;

        if (maxAttempts <= 0)
        {
            throw new IllegalArgumentException("maxAttempts must be positive");
        }
        if (ttl.isZero() || ttl.isNegative())
        {
            throw new IllegalArgumentException("ttl must be positive");
        }

        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(daemonThreadFactory());

        if (startCleanupScheduler)
        {
            this.cleanupExecutor.scheduleAtFixedRate(
                    this::safeCleanup,
                    CLEANUP_INTERVAL.toMillis(),
                    CLEANUP_INTERVAL.toMillis(),
                    TimeUnit.MILLISECONDS
            );
        }
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Creates (or replaces) an OTP for the given email.
     *
     * @param email  raw email from request
     * @param userId may be null if you prefer not to bind user at issue time
     * @return raw OTP digits for out-of-band delivery (email/log). Never persist this return value.
     */
    public String issue(String email, UUID userId)
    {
        ensureOpen();

        String key = normalizeEmail(email);
        if (key.isEmpty())
        {
            throw new IllegalArgumentException("email cannot be blank");
        }

        String rawOtp = generateNumericOtp(OTP_LENGTH);
        String codeHash = passwordHasher.hash(rawOtp);
        OffsetDateTime now = timeProvider.now();
        OffsetDateTime expiresAt = now.plus(ttl);

        OtpEntry entry = OtpEntry.create(userId, codeHash, now, expiresAt);
        store.put(key, entry);

        return rawOtp;
    }

    /**
     * Validates OTP without consuming it (step: VerifyPasswordResetCode).
     * Failed attempts increment attemptCount.
     * Successful verify marks entry as verified (still kept until consume/invalidate).
     */
    public OtpVerifyStatus verify(String email, String rawCode)
    {
        ensureOpen();
        String key = normalizeEmail(email);
        if (key.isEmpty() || rawCode == null || rawCode.isBlank())
        {
            return OtpVerifyStatus.INVALID_CODE;
        }

        final OtpVerifyStatus[] status = {OtpVerifyStatus.NOT_FOUND};

        store.computeIfPresent(key, (k, current) ->
        {
            OffsetDateTime now = timeProvider.now();

            if (current.isExpired(now))
            {
                status[0] = OtpVerifyStatus.EXPIRED;
                return null; // drop expired
            }

            if (!current.hasAttemptsLeft(maxAttempts))
            {
                status[0] = OtpVerifyStatus.TOO_MANY_ATTEMPTS;
                return current;
            }

            if (!passwordHasher.verify(rawCode.trim(), current.getCodeHash()))
            {
                status[0] = OtpVerifyStatus.INVALID_CODE;
                OtpEntry updated = current.withIncrementedAttempt();
                // if this attempt exhausted the limit, keep entry so next calls still get TOO_MANY_ATTEMPTS
                return updated;
            }

            status[0] = OtpVerifyStatus.OK;
            return current.markVerified();
        });

        return status[0];
    }

    /**
     * Validates OTP and removes it atomically on success (step: ResetPassword).
     * Prefer this when reset is a single shot after code entry.
     *
     * If you use a separate verify step, you may require entry.isVerified()
     * or re-check the code here for safety.
     */
    public ConsumeResult consume(String email, String rawCode)
    {
        ensureOpen();

        String key = normalizeEmail(email);
        if (key.isEmpty() || rawCode == null || rawCode.isBlank())
        {
            return ConsumeResult.failed(OtpVerifyStatus.INVALID_CODE, null);
        }

        final ConsumeResult[] result = {
                ConsumeResult.failed(OtpVerifyStatus.NOT_FOUND, null)
        };

        store.compute(key, (k, current) ->
        {
            if (current == null)
            {
                result[0] = ConsumeResult.failed(OtpVerifyStatus.NOT_FOUND, null);
                return null;
            }

            OffsetDateTime now = timeProvider.now();

            if (current.isExpired(now))
            {
                result[0] = ConsumeResult.failed(OtpVerifyStatus.EXPIRED, null);
                return null;
            }

            if (!current.hasAttemptsLeft(maxAttempts))
            {
                result[0] = ConsumeResult.failed(OtpVerifyStatus.TOO_MANY_ATTEMPTS, current.getUserId());
                return current;
            }

            if (!passwordHasher.verify(rawCode.trim(), current.getCodeHash()))
            {
                OtpEntry updated = current.withIncrementedAttempt();
                result[0] = ConsumeResult.failed(OtpVerifyStatus.INVALID_CODE, updated.getUserId());
                return updated;
            }

            // success → remove (return null)
            result[0] = ConsumeResult.ok(current.getUserId());
            return null;
        });

        return result[0];
    }

    /**
     * Consumes only if previously verified (3-step flow: request → verify → reset).
     * Does not re-check OTP digits; reset step only needs email + new password
     * if product allows that. Safer variant: still pass code and use consume().
     */
    public ConsumeResult consumeIfVerified(String email)
    {
        ensureOpen();

        System.out.println("consumeIfVerified");
        String key = normalizeEmail(email);
        if (key.isEmpty())
        {
            return ConsumeResult.failed(OtpVerifyStatus.INVALID_CODE, null);
        }

        final ConsumeResult[] result = {
                ConsumeResult.failed(OtpVerifyStatus.NOT_FOUND, null)
        };

        store.compute(key, (k, current) ->
        {
            if (current == null)
            {
                result[0] = ConsumeResult.failed(OtpVerifyStatus.NOT_FOUND, null);
                return null;
            }

            OffsetDateTime now = timeProvider.now();
            if (current.isExpired(now))
            {
                result[0] = ConsumeResult.failed(OtpVerifyStatus.EXPIRED, null);
                return null;
            }

            if (!current.isVerified())
            {
                // reuse INVALID_CODE as "not verified yet"
                result[0] = ConsumeResult.failed(OtpVerifyStatus.INVALID_CODE, current.getUserId());
                return current;
            }

            result[0] = ConsumeResult.ok(current.getUserId());
            return null;
        });

        return result[0];
    }

    public void invalidate(String email)
    {
        ensureOpen();
        String key = normalizeEmail(email);
        if (!key.isEmpty())
        {
            store.remove(key);
        }
    }

    public Optional<UUID> consumeVerifiedUserId(String email) {
        String key = normalizeEmail(email);
        OffsetDateTime now = timeProvider.now();

        AtomicReference<UUID> result = new AtomicReference<>();

        store.compute(key, (k, current) -> {
            if (current == null) {
                return null;
            }

            if (current.isExpired(now)) {
                return null;
            }

            if (!current.isVerified()) {
                return current;
            }

            result.set(current.getUserId());
            return null; // consume
        });

        return Optional.ofNullable(result.get());
    }


    public Optional<UUID> getVerifiedUserId(String email) {
        System.out.println("getVerifiedUserId");
        String key = normalizeEmail(email);
        OffsetDateTime now = timeProvider.now();

        System.out.println("get entry");
        OtpEntry entry = store.get(key);
        if (entry == null) {
            return Optional.empty();
        }

        if (entry.isExpired(now)) {
            store.remove(key, entry);
            return Optional.empty();
        }

        if (!entry.isVerified()) {
            return Optional.empty();
        }
        System.out.println(2);
        return Optional.of(entry.getUserId());
    }


    public Optional<OtpEntry> peek(String email)
    {
        ensureOpen();
        String key = normalizeEmail(email);
        if (key.isEmpty())
        {
            return Optional.empty();
        }

        OtpEntry entry = store.get(key);
        if (entry == null)
        {
            return Optional.empty();
        }

        if (entry.isExpired(timeProvider.now()))
        {
            store.remove(key, entry);
            return Optional.empty();
        }

        return Optional.of(entry);
    }

    /** Visible for tests / metrics */
    public int size()
    {
        return store.size();
    }

    public void cleanup()
    {
        OffsetDateTime now = timeProvider.now();
        store.entrySet().removeIf(e -> e.getValue().isExpired(now));
    }

    @Override
    public void close()
    {
        if (closed.compareAndSet(false, true))
        {
            cleanupExecutor.shutdownNow();
            store.clear();
        }
    }

    // -------------------------------------------------------------------------
    // Internals
    // -------------------------------------------------------------------------

    public static String normalizeEmail(String email)
    {
        if (email == null)
        {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateNumericOtp(int length)
    {
        int bound = (int) Math.pow(10, length);
        int number = secureRandom.nextInt(bound);
        return String.format(Locale.ROOT, "%0" + length + "d", number);
    }

    private void safeCleanup()
    {
        try
        {
            if (!closed.get())
            {
                cleanup();
            }
        }
        catch (Exception ignored)
        {
            // never let cleanup kill the scheduler thread silently without isolation
        }
    }

    private void ensureOpen()
    {
        if (closed.get())
        {
            throw new IllegalStateException("PasswordResetOtpService is closed");
        }
    }

    private static ThreadFactory daemonThreadFactory()
    {
        return runnable ->
        {
            Thread t = new Thread(runnable, "password-reset-otp-cleanup");
            t.setDaemon(true);
            return t;
        };
    }

    // -------------------------------------------------------------------------
    // Result types
    // -------------------------------------------------------------------------

    public static final class ConsumeResult
    {
        private final boolean success;
        private final OtpVerifyStatus status;
        private final UUID userId;

        private ConsumeResult(boolean success, OtpVerifyStatus status, UUID userId)
        {
            this.success = success;
            this.status = status;
            this.userId = userId;
        }

        public static ConsumeResult ok(UUID userId)
        {
            return new ConsumeResult(true, OtpVerifyStatus.OK, userId);
        }

        public static ConsumeResult failed(OtpVerifyStatus status, UUID userId)
        {
            return new ConsumeResult(false, status, userId);
        }

        public boolean isSuccess()
        {
            return success;
        }

        public OtpVerifyStatus getStatus()
        {
            return status;
        }

        public Optional<UUID> getUserId()
        {
            return Optional.ofNullable(userId);
        }
    }
}
