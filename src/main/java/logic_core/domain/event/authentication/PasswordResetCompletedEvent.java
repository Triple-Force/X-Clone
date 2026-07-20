package logic_core.domain.event.authentication;

import logic_core.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class PasswordResetCompletedEvent extends DomainEvent implements AuthenticationEvent
{
    private final UUID userId;
    private final String email;

    public PasswordResetCompletedEvent(UUID userId,
                                       String email,
                                       OffsetDateTime occurredAt)
    {
        super(occurredAt);
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
    }
}