package logic_core.app.service.passwordReset;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * In-memory password-reset OTP state for one email.
 * Raw OTP is never stored; only its hash.
 */
public final class OtpEntry
{
    private final UUID userId;              // optional but useful for reset without second lookup race
    private final String codeHash;
    private final OffsetDateTime expiresAt;
    private final int attemptCount;
    private final boolean verified;         // true after successful verify step (optional 3-step flow)
    private final OffsetDateTime createdAt;

    public OtpEntry(
            UUID userId,
            String codeHash,
            OffsetDateTime expiresAt,
            int attemptCount,
            boolean verified,
            OffsetDateTime createdAt)
    {
        this.userId = userId;
        this.codeHash = Objects.requireNonNull(codeHash, "codeHash");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
        this.attemptCount = attemptCount;
        this.verified = verified;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public static OtpEntry create(
            UUID userId,
            String codeHash,
            OffsetDateTime now,
            OffsetDateTime expiresAt)
    {
        return new OtpEntry(userId, codeHash, expiresAt, 0, false, now);
    }

    public UUID getUserId()
    {
        return userId;
    }

    public String getCodeHash()
    {
        return codeHash;
    }

    public OffsetDateTime getExpiresAt()
    {
        return expiresAt;
    }

    public int getAttemptCount()
    {
        return attemptCount;
    }

    public boolean isVerified()
    {
        return verified;
    }

    public OffsetDateTime getCreatedAt()
    {
        return createdAt;
    }

    public boolean isExpired(OffsetDateTime now)
    {
        return !expiresAt.isAfter(now); // expiresAt <= now
    }

    public boolean hasAttemptsLeft(int maxAttempts)
    {
        return attemptCount < maxAttempts;
    }

    public OtpEntry withIncrementedAttempt()
    {
        return new OtpEntry(
                userId,
                codeHash,
                expiresAt,
                attemptCount + 1,
                verified,
                createdAt
        );
    }

    public OtpEntry markVerified()
    {
        return new OtpEntry(
                userId,
                codeHash,
                expiresAt,
                attemptCount,
                true,
                createdAt
        );
    }
}
